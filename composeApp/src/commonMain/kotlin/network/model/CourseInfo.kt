package network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class CourseInfo(
    val buildingName: String?,
    val classIds: String,
    val classNames: String,
    val classType: String,
    val courseId: String,
    val courseName: String,
    val currentDate: String,
    val currentDay: String,
    val currentWeek: String,
    val id: String,
    val isAction: String?,
    val isAllowDownload: String?,
    val isNowPlay: String,
    val isOpen: String?,
    val livePath: String,
    val liveRecordName: String,
    val liveStatus: String,
    val resourceId: String?,
    val roomId: String?,
    val roomName: String?,
    val roomType: String?,
    val schImgUrl: String,
    val scheduleTimeEnd: String,
    val scheduleTimeStart: String,
    val section: String,
    val teacherName: String,
    val timeRange: String,
    val videoClassMap: VideoClassMap,
    val videoPath: String,
    val videoTimes: String
) {
    @Serializable(with = VideoClassMapSerializer::class)
    class VideoClassMap(map: Map<String, String> = mapOf()) : Map<String, String> by map

    val formatSchedule: String
        get() = "${currentWeek}周 星期${currentDay}$timeRange"

    object VideoClassMapSerializer : kotlinx.serialization.KSerializer<VideoClassMap> {
        override val descriptor: SerialDescriptor
            get() = serialDescriptor<List<Map<String, String>>>()

        override fun deserialize(decoder: Decoder): VideoClassMap {
            val json = decoder.decodeSerializableValue(JsonElement.serializer())
            if (json is JsonNull) return VideoClassMap()
            val map = json.jsonArray.map {
                (it.jsonObject["videoName"]!!.jsonPrimitive.content) to it.jsonObject["videoClassId"]!!.jsonPrimitive.content
            }.toMap()
            return VideoClassMap(map)

        }

        override fun serialize(encoder: Encoder, value: VideoClassMap) {
            TODO("Not yet implemented")
        }

    }
}
