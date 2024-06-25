package network.model

import kotlinx.serialization.Serializable

@Serializable
data class ListResponse<T>(
    val dataList: List<T>,
    val pageConfig:PageConfig?=null
){
    @Serializable
    data class PageConfig(
        val totalPage: Int,
        val pageSize: Int,
        val page: Int,
        val totalCount: Int
    )
}