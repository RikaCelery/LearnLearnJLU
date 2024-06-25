package ui.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import network.Http
import network.model.CourseInfo
import service.ILearnTech

data class CourseVideoCard(
    val courseName: String,
    val courseTeacher: String,
    val courseSchedule: String,
    val courseLocation: String?,
    val courseTerm: String,
    val id: String,
    val resourceId: String? = null
) {
    val display: String
        get() = "$courseName ($courseTeacher)"

}

class CourseVideoCardVM(card: CourseVideoCard) : ViewModel() {
    constructor(card: CourseInfo, term: String, termId: String) : this(
        CourseVideoCard(
            courseName = card.courseName,
            courseTeacher = card.teacherName,
            courseTerm = "$term-$termId",
            courseLocation = card.roomName,
            courseSchedule = card.formatSchedule,
            id = card.id,
            resourceId = card.resourceId
        )
    )

    val id
        get() = uiState.value.id
    private val _uiState = MutableStateFlow(card)
    val uiState: StateFlow<CourseVideoCard> = _uiState.asStateFlow()
    fun click() {
        GlobalScope.launch(Dispatchers.IO) {
            val info = ILearnTech.queryDownloadInfo(Http.client,_uiState.value.resourceId!!)
            val file =  Http.splitDownload(info.videoList[1].videoPath){ index, current, total ->

            }
            println(file)
        }
        //todo
    }

    fun longClick() {
        //todo
    }
}