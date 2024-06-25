package network.model


import kotlinx.serialization.Serializable

@Serializable
data class CourseVideoInfo(
    val audioPath: String = "",
    val buildingName: String = "", // xx教学楼
    val classNames: String = "", // xxx
    val classType: Int = 0, // 4
    val commentStatus: Int = 0, // 1
    val company: Int = 0, // 4
    val courseId: String = "", // xx
    val createId: String = "", // xx
    val detectKnowledgeStatus: Int = 0, // 1
    val enableWater: Int = 0, // 1
    val isPublish: Int = 0, // 2
    val liveRecordId: String = "", // xx
    val phaseUrl: String = "",
    val resourceCover: String = "", // xxx
    val resourceName: String = "", // xx_直播回放
    val resourceType: Int = 0, // 1
    val roomName: String = "", // xxx
    val scheduleId: String = "", // xx
    val teacherIds: String = "", // xx
    val teacherList: List<String> = listOf(),
    val teacherName: String = "", // xxx
    val transPhaseStatus: Int = 0, // 1
    val videoList: List<Video> = listOf()
) {
    @Serializable
    data class Video(
        val id: String = "", // xxx
        val videoCode: String = "", // 1
        val videoName: String = "", // 教师机位
        val videoPath: String = "", // xxx
        val videoSize: String = "" // 1537086461
    )
}