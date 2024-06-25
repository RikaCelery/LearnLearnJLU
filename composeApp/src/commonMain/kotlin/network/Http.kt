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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.createTempFile

object Http : AutoCloseable {
    val client = HttpClient(OkHttp) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
        }
        install(DefaultRequest) {
            headers {
                set("Accept", "*/*")
                set("Accept-Charset", "*")
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                prettyPrint = true
            })
        }
        install(HttpRedirect) {
            checkHttpMethod = false
            allowHttpsDowngrade = true
        }
        install(HttpCookies)
        install(UserAgent) {
            agent =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
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

    suspend fun splitDownload(
        url: String, progressing: (index: Int,current:Long, total:Long) -> Unit
    ): File {

        val response = client.head(url)
        require(response.headers["Accept-Ranges"] == "bytes") { "Range not supported" }
        requireNotNull(response.headers["Content-Length"]) { "Content-Length not found" }
        val contentLength = response.headers["Content-Length"]!!.toLong()
        val chunkSize = 1024 * 1024 * 30
        val saveTo = createTempFile("split", "tmp")
        val os = saveTo.toFile().outputStream()
        supervisorScope {
            (0 until contentLength / chunkSize)
                .map { it * chunkSize..<(it.plus(1) * chunkSize).coerceAtMost(contentLength - 1) }
                .mapIndexed { index, range ->
                    async(Dispatchers.IO) {
                        println("downloading $index")
                        index to downloadRange(url, range) {
                            val file = createTempFile("split", "tmp").toFile()
                            val buf = ByteArray(1024)
                            val fileOutputStream = file.outputStream()
                            bodyAsChannel().toInputStream().use {
                                val total = range.last - range.first + 1
                                var sum = 0L
                                while (true) {
                                    val len = it.read(buf)
                                    if (len == -1) break
                                    fileOutputStream.write(buf, 0, len)
                                    sum += len
                                    progressing(index,sum,total)
                                }
                            }
                            fileOutputStream.close()
                            println("downloaded $index")
                            file
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
        os.close()
        return saveTo.toFile()
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun <R> downloadRange(url: String, range: LongRange, writer: (suspend HttpResponse.() -> R)? = null): R? {
        contract {

            returns(null) implies (writer == null)
            returnsNotNull() implies (writer != null)
        }
        println("Downloading Range: $range")
        val response = client.get(url) {
            headers {
                append("Range", "bytes=${range.first}-${range.last}")
            }
        }
        return writer?.invoke(response)
    }
}