package ui.vm

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import network.model.CourseInfo
import service.DownloadManager

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

    private val _teacherVideoDownloadInfo = MutableStateFlow(mutableListOf<SegmentDownloadInfo>())
    private fun setTeacherVideoDownloadInfo(new: List<SegmentDownloadInfo>) {
        _teacherVideoDownloadInfo.update {
            new.toMutableStateList()
        }
    }
    val teacherVideoDownloadInfo = _teacherVideoDownloadInfo.asStateFlow()

    private val _computerVideoDownloadInfo = MutableStateFlow(mutableListOf<SegmentDownloadInfo>())
    private fun setComputerDownloadInfo(new: List<SegmentDownloadInfo>) {
        _computerVideoDownloadInfo.update {
            new.toMutableStateList()
        }
    }
    val computerVideoDownloadInfo = _computerVideoDownloadInfo.asStateFlow()

    val id
        get() = uiState.value.id
    private val scope = CoroutineScope(SupervisorJob())
    private val _uiState = MutableStateFlow(card)
    val uiState: StateFlow<CourseVideoCard> = _uiState.asStateFlow()

    fun click() {
        //todo
    }

    fun downloadHdmi() {
        DownloadManager.submit(id + "hdmi", uiState.value.resourceId!!, "HDMI", { new ->
            setComputerDownloadInfo(new)
        }) { file ->
//            file.renameTo()
        }
    }

    fun downloadTeacher() {
        DownloadManager.submit(id + "teacher", uiState.value.resourceId!!, "教师机位", { new ->
            setComputerDownloadInfo(new)
        }) { file ->
//            file.renameTo()
        }
    }

    fun longClick() {
        //todo
    }

    fun init() {
        DownloadManager.register(id + "hdmi") { list: List<SegmentDownloadInfo> ->
            setComputerDownloadInfo(list)
        }
        DownloadManager.register(id + "teacher") { list: List<SegmentDownloadInfo> ->
            setTeacherVideoDownloadInfo(list)
        }
    }

    init {
        init()
        println("init" + id)
    }
}