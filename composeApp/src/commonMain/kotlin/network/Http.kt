@file:Suppress("unused")

package network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.Dns
import util.serialization.JSON
import java.io.File
import java.net.InetAddress
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.createTempFile

object Http : AutoCloseable {
    val client = HttpClient(OkHttp) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.NONE
        }
        install(DefaultRequest) {
            headers {
                set("Accept", "*/*")
                set("Accept-Charset", "*")
            }
        }
        install(ContentNegotiation) {
            json(JSON)
        }
        install(HttpRedirect) {
            checkHttpMethod = false
            allowHttpsDowngrade = true
        }
        install(HttpCookies)
        install(UserAgent) {
            agent =
                "Mozilla/5.0 Chrome/126.0.0.0 Safari/537.36"
        }
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 5)
            constantDelay(300, 2000)
        }
        install(HttpTimeout) {
            socketTimeoutMillis = Long.MAX_VALUE
            connectTimeoutMillis = 15_000
        }
        engine {
            config {
                dns(object : Dns {
                    override fun lookup(hostname: String): List<InetAddress> {
                        if (hostname == "jwcmedia.jlu.edu.cn") /*36.132.218.94*/
                        return listOf(InetAddress.getByAddress("jwcmedia.jlu.edu.cn",byteArrayOf(36, 132.toByte(), 218.toByte(), 94)))
                        return Dns.SYSTEM.lookup(hostname)
                    }
                })
                sslSocketFactory(OkHttpUtil.ignoreInitedSslFactory, OkHttpUtil.IGNORE_SSL_TRUST_MANAGER_X509)
                hostnameVerifier(OkHttpUtil.ignoreSslHostnameVerifier)
            }
        }
    }

    override fun close() {
        client.close()
    }

    suspend fun get(url: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        return client.get(url, block)
    }

    suspend fun post(url: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        return client.post(url, block)
    }

    suspend fun postForm(url: String, form: Map<String, Any>, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        return client.submitForm(url, formParameters = parameters {
            form.forEach { (k, v) ->
                append(k, v.toString())
            }
        }, block = block)
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun splitDownload(
        url: String,
        length: Long? = null,
        beforeStart: suspend (List<LongRange>) -> Unit = {},
        progressing: suspend (index: Int, current: Long, total: Long) -> Unit = { _, _, _ -> }
    ): File {
        contract {
            callsInPlace(beforeStart, InvocationKind.EXACTLY_ONCE)
        }
        val response = client.head(url)
        require(response.headers["Accept-Ranges"] == "bytes") { "Range not supported" }
        requireNotNull(response.headers["Content-Length"]) { "Content-Length not found" }
        val contentLength = length ?: response.headers["Content-Length"]!!.toLong()
        require(contentLength > 1024 * 1024) { "文件太小，不支持下载" + client.get(url).bodyAsText() }
        val chunkSize = 1024 * 1024 * 50
        val saveTo = createTempFile("split", "tmp")
        val os = saveTo.toFile().outputStream()
        supervisorScope {
            val gate = Semaphore(8)

            (0..contentLength / chunkSize)
                .map { it * chunkSize..<(it.plus(1) * chunkSize).coerceAtMost(contentLength - 1) }
                .also {
                    beforeStart(it)
                }
                .mapIndexed { index, range ->
                    gate.withPermit {
                        async(Dispatchers.IO) {
                            println("downloading $index")
                            index to downloadRange(url, range, { sum, total ->
                                progressing(index, sum, total)
                            }) {
                                val file = createTempFile("split", "tmp").toFile()
                                val buf = ByteArray(1024)
                                val fileOutputStream = file.outputStream()
                                bodyAsChannel().toInputStream().use {
                                    while (true) {
                                        val len = it.read(buf)
                                        if (len == -1) break
                                        fileOutputStream.write(buf, 0, len)
                                    }
                                }
                                fileOutputStream.close()
                                println("downloaded $index")
                                file
                            }
                        }
                    }
                }.awaitAll().sortedBy { it.first }.forEach {
                    it.second?.inputStream()?.use {
                        val buf = ByteArray(1024)
                        while (true) {
                            val len = it.read(buf)
                            if (len == -1) break
                            os.write(buf, 0, len)
                        }
                    }
                    it.second?.delete()
                }
        }
        runBlocking(Dispatchers.IO) { os.close() }
        return saveTo.toFile()
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun <R> downloadRange(
        url: String,
        range: LongRange,
        progressing: suspend (Long, Long) -> Unit = { _, _ -> },
        writer: (suspend HttpResponse.() -> R)? = null
    ): R? {
        contract {
            returns(null) implies (writer == null)
            returnsNotNull() implies (writer != null)
        }
        println("Downloading Range: $range")
        val request = client.prepareGet(url) {
            headers {
                append("Range", "bytes=${range.first}-${range.last}")
            }
            onDownload { bytesSentTotal, contentLength ->
                progressing.invoke(bytesSentTotal, contentLength)
            }
        }

        return request.execute { resp ->
            writer?.invoke(resp)
        }
    }
}