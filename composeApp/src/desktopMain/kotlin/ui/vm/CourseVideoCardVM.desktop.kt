package ui.vm

import util.Config
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.io.path.exists

actual fun saveFile(file: File) {
    val downloadFolder = Path(System.getProperty("user.home")).resolve("Downloads")
    Files.move(file.toPath(), downloadFolder.resolve(file.name), StandardCopyOption.REPLACE_EXISTING)
}

actual fun isDestFileExist(courseTerm: String, courseName: String, courseSchedule: String, videoType: String): Boolean {

//    if (OsUtils.isMAC) {
    val downloadFolder = Path(System.getProperty("user.home")).resolve("Downloads")
    return downloadFolder.resolve(Config.formatName(courseTerm, courseName, courseSchedule, videoType)).exists()
//    }else{
//
//    }

}