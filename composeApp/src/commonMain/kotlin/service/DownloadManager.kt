package service

import kotlinx.coroutines.*
import network.Http
import ui.vm.SegmentDownloadInfo
import java.io.File
import java.util.*

object DownloadManager {
    private val scope = CoroutineScope(SupervisorJob())
    private val callBacks =
        Hashtable<String, (List<SegmentDownloadInfo>) -> Unit>()
    private val jobs = Hashtable<String, Job>()
    private val segments = Hashtable<String, MutableList<SegmentDownloadInfo>>()
    fun submit(
        key: String,
        resourceId: String,
        videoType: String,
        onSegmentInfoInitialized: (List<SegmentDownloadInfo>) -> Unit,
        onDownloadSuccess: (File) -> Unit,
    ) {
        if (jobs.contains(key)) return
        jobs.set(key, scope.launch(Dispatchers.IO) {
            val info = ILearnTech.queryDownloadInfo(Http.client, resourceId)
            val video = info.videoList.singleOrNull { it.videoName == videoType }
            requireNotNull(video)
            val file = Http.splitDownload(video.videoPath, video.videoSize, {
                val toMutableList = it.mapIndexed { index: Int, longRange: LongRange ->
                    SegmentDownloadInfo(index, 0, longRange.last - longRange.first)
                }.toMutableList()
                segments[key] = toMutableList
                onSegmentInfoInitialized(toMutableList)
            }) { index, current, total ->
                segments[key]!![index] = SegmentDownloadInfo(index, current, total)
                callBacks[key]?.invoke(segments[key]!!)

            }
            onDownloadSuccess(file)
        })
    }

    fun register(
        id: String,
        function2: (List<SegmentDownloadInfo>) -> Unit
    ) {
        callBacks[id] = function2
        if (segments.containsKey(id)) {
            val infos = segments[id]!!
            scope.launch {
                function2(infos)
            }
        }
    }
}