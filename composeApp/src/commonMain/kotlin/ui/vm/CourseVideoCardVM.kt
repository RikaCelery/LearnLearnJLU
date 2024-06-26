package ui.vm

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import network.model.CourseInfo
import service.DownloadManager
import util.Config
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

data class CourseVideoCard(
    val courseName: String,
    val courseTeacher: String,
    val courseSchedule: String,
    val courseLocation: String?,
    val courseTerm: String,
    val id: String,
    val resourceId: String? = null,
    val streams: List<String> = listOf<String>(),
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

    private val _uiState = MutableStateFlow(card)
    val uiState: StateFlow<CourseVideoCard> = _uiState.asStateFlow()

    fun click() {
        //todo
    }

    fun downloadHdmi() {
        val value = uiState.value
        if (isDestFileExist(value.courseTerm, value.courseName, value.courseSchedule, "HDMI")) {
            setComputerDownloadInfo(listOf(SegmentDownloadInfo(0,1,1)))
            return
        }
        DownloadManager.submit(id + "hdmi", value.resourceId!!, "HDMI", { new ->
            setComputerDownloadInfo(new)
        }) { file ->
            val dest = file.parentFile.toPath()
                .resolve(
                    Config.formatName(value.courseTerm,value.courseName, value.courseSchedule, "HDMI")
                )
            Files.move(
                file.toPath(),
                dest, StandardCopyOption.REPLACE_EXISTING
            )
            saveFile(dest.toFile())
        }
    }


    fun downloadTeacher() {
        val value = uiState.value
        if (isDestFileExist(value.courseTerm, value.courseName, value.courseSchedule, "教师机位")) {
            setTeacherVideoDownloadInfo(listOf(SegmentDownloadInfo(0,1,1)))
            return
        }
        DownloadManager.submit(id + "teacher", value.resourceId!!, "教师机位", { new ->
            setTeacherVideoDownloadInfo(new)
        }) { file ->
            val dest = file.parentFile.toPath()
                .resolve(
                    Config.formatName(value.courseTerm,value.courseName, value.courseSchedule, "教师机位")
                )
            Files.move(
                file.toPath(),
                dest, StandardCopyOption.REPLACE_EXISTING
            )
            saveFile(dest.toFile())
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
        val value = uiState.value
        if (isDestFileExist(value.courseTerm, value.courseName, value.courseSchedule, "HDMI")) {
            setComputerDownloadInfo(listOf(SegmentDownloadInfo(0,1,1)))
            return
        }
        if (isDestFileExist(value.courseTerm, value.courseName, value.courseSchedule, "教师机位")) {
            setTeacherVideoDownloadInfo(listOf(SegmentDownloadInfo(0,1,1)))
            return
        }
    }

    init {
        init()
    }
}

expect fun saveFile(file: File)
expect fun isDestFileExist(courseTerm: String, courseName: String, courseSchedule: String,videoType:String):Boolean
