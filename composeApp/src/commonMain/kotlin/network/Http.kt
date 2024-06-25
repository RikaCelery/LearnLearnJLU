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
import kotlinx.serialization.json.Json

object Http:AutoCloseable {
    val client = HttpClient(OkHttp) {
        install (Logging) {
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
    suspend fun postForm(url: String,form: Map<String,Any>, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        return client.submitForm(url, formParameters = parameters {
            form.forEach { (k, v) ->
                append(k, v.toString())
            }
        }, block =  block)
    }
}