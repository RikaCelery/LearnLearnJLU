package network.model


import kotlinx.serialization.Serializable

@Serializable
data class LessonInfo(
    val classId: String = "", // xxx
    val classroomId: String = "", // xxx
    val courseId: String = "", // xxx
    val courseName: String = "", // xxx
    val cover: String = "", // https://ilearn.jlu.edu.cn/iplat/upload/course/202303/xxx
    val id: String = "", // xxx
    val name: String = "", // xxx
    val schoolId: String = "", // 53
    val schoolName: String = "", // null
    val status: String = "", // 2
    val statusName: String = "", // 已结束
    val studentId: String = "", // xxx
    val teacherId: String = "", // xxx
    val teacherName: String = "", // xxx
    val teacherUsername: String = "", // xxx
)
