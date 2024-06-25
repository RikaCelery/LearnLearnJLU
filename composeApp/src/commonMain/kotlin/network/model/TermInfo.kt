package network.model

import kotlinx.serialization.Serializable

@Serializable
data class TermInfo(
    val endDate: String,
    val id: String,
    val name: String,
    val num: String,
    val selected: String,
    val startDate: String,
    val year: String
)