package util.http

import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonElement

suspend fun HttpResponse.json(): JsonElement {
    return this.body<JsonElement>()
}