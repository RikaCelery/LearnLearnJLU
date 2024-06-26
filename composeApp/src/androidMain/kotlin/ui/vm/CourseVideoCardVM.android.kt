package ui.vm

import android.os.Environment
import github.rikacelery.learnlearnjlu.MainActivity
import util.Config
import java.io.File

actual fun saveFile(file: File) {
    MainActivity.INSTANCE.saveFile(file)
}

actual fun isDestFileExist(courseTerm: String, courseName: String, courseSchedule: String, videoType: String): Boolean {
    val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    return downloadFolder.resolve(Config.formatName(courseTerm, courseName, courseSchedule, videoType)).exists()
}