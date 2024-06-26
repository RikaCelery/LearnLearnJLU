package util.serialization

import kotlinx.serialization.json.Json

val JSON = Json {
    coerceInputValues = true
    isLenient =true
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true

}