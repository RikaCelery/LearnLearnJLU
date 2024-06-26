package util

object Config {

    fun formatName(courseTerm: String, courseName: String, courseSchedule: String,videoType:String) =
        "$courseTerm $courseName $courseSchedule $videoType" + ".mp4"
            .replace("/\\*<>\\|:%".toRegex(), "_")
}