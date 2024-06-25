package network.model

import kotlinx.serialization.Serializable

@Serializable
data class JsonResponse<T>(
    val data:T?=null,
    val code:Int?=null,
    val status:Int,
    val message:String,
    val location:String?,
)
