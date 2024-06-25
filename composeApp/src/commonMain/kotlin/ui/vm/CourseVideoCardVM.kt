package ui.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import network.model.VideoClassInfo

data class CourseVideoCard(
    val courseName: String,
    val courseTeacher: String,
    val courseSchedule: String,
    val courseLocation: String?,
    val courseTerm: String,
    val id: String,
    val streams: VideoClassInfo.VideoClassMap = VideoClassInfo.VideoClassMap()
) {
    val display: String
        get() = "$courseName ($courseTeacher)"

}

class CourseVideoCardVM(card: CourseVideoCard) : ViewModel() {
    constructor(card: VideoClassInfo, term: String, termId: String) : this(
        CourseVideoCard(
            courseName = card.courseName,
            courseTeacher = card.teacherName,
            courseTerm = "$term-$termId",
            courseLocation = card.roomName,
            courseSchedule = card.formatSchedule,
            id = card.id,
            streams = card.videoClassMap
        )
    )

    val id
        get() = uiState.value.id
    private val _uiState = MutableStateFlow(card)
    val uiState: StateFlow<CourseVideoCard> = _uiState.asStateFlow()
    fun click() {
        //todo
    }

    fun longClick() {
        //todo
    }
}