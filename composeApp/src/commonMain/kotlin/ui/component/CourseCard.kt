package ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import ui.vm.CourseVideoCardVM
import ui.vm.SegmentDownloadInfo
import util.calculateY

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseCard(
    vm: CourseVideoCardVM
) {
    LaunchedEffect(Unit) {
        vm.init()
    }
    val state by vm.uiState.collectAsState()
    val computerVideoDownloadInfo by vm.computerVideoDownloadInfo.collectAsState()
    val teacherVideoDownloadInfo by vm.teacherVideoDownloadInfo.collectAsState()
    Card(Modifier.fillMaxWidth().padding(10.dp, 5.dp)) {
        ConstraintLayout(Modifier.drawBehind {
            val teacherSum = teacherVideoDownloadInfo.sumOf { it.segmentLength }
            val computerSum = computerVideoDownloadInfo.sumOf { it.segmentLength }

            computerVideoDownloadInfo.fold(SegmentDownloadInfo(0, 0, 0)) { acc, item ->
                val offsetPercent = acc.segmentLength.toFloat() / computerSum
                val blockSizePercentage = item.segmentLength.toFloat() / computerSum
                val percent = blockSizePercentage * item.segmentFinished / item.segmentLength
                drawRect(
                    Color.hsv(
                        calculateY(percent / blockSizePercentage) * 120,
                        1f,
                        1f,
                        if (item.segmentFinished >= item.segmentLength - 1) .8f else .2f
                    ),
                    topLeft = Offset(size.width * offsetPercent, size.height / 2),
                    size = Size(percent * size.width, size.height / 2)
                )
                drawRect(
                    Color(0x22000000),
                    topLeft = Offset(
                        size.width * (offsetPercent + percent),
                        size.height / 2
                    ),
                    size = Size((blockSizePercentage - percent) * size.width, size.height / 2)
                )
                SegmentDownloadInfo(item.segmentIndex, item.segmentFinished, item.segmentLength + acc.segmentLength)
            }
            teacherVideoDownloadInfo.fold(SegmentDownloadInfo(0, 0, 0)) { acc, item ->
                val offsetPercent = acc.segmentLength.toFloat() / teacherSum
                val blockSizePercentage = item.segmentLength.toFloat() / teacherSum
                val percent = blockSizePercentage * item.segmentFinished / item.segmentLength
                drawRect(
                    Color.hsv(
                        calculateY(percent / blockSizePercentage) * 120,
                        1f,
                        1f,
                        if (item.segmentFinished >= item.segmentLength - 1) .8f else .2f
                    ),
                    topLeft = Offset(size.width * offsetPercent, size.height / 2),
                    size = Size(percent * size.width, size.height / 2)
                )
                drawRect(
                    Color(0x22000000),
                    topLeft = Offset(
                        size.width * (offsetPercent + percent),
                        size.height / 2
                    ),
                    size = Size((blockSizePercentage - percent) * size.width, size.height / 2)
                )
                SegmentDownloadInfo(item.segmentIndex, item.segmentFinished, item.segmentLength + acc.segmentLength)
            }
        }.fillMaxWidth().combinedClickable(onClick = { vm.click() }, onLongClick = { vm.longClick() })
            .padding(5.dp)
        ) {
            val (topLine, time, locationIcon, location) = createRefs()
            Row(Modifier.fillMaxWidth().constrainAs(topLine) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }, verticalAlignment = Alignment.CenterVertically) {
                Text(state.display, Modifier, maxLines = 1)
                Text(
                    state.courseTerm,
                    Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0x99ffaa00)).padding(2.dp, 1.dp),
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            Text(
                state.courseSchedule,
                Modifier.constrainAs(time) {
                    top.linkTo(topLine.bottom, 4.dp)
                    start.linkTo(parent.start)
                },
                fontSize = 14.sp,
            )
            Icon(
                Icons.Filled.LocationOn, null,
                Modifier.constrainAs(locationIcon) {
                    top.linkTo(time.bottom, 4.dp)
                    start.linkTo(parent.start)
                }.height(20.dp),
            )
            Text(
                state.courseLocation?: "[教室不详]",
                Modifier.constrainAs(location) {
                    top.linkTo(time.bottom)
                    start.linkTo(locationIcon.end)
                },
                fontSize = 14.sp
            )
        }

    }
}