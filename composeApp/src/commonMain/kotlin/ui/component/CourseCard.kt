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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import ui.vm.CourseVideoCardVM

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseCard(
    cardVM: CourseVideoCardVM
) {
    val state by cardVM.uiState.collectAsState()
    Card(Modifier.fillMaxWidth().padding(10.dp,5.dp)) {
        ConstraintLayout(
            Modifier.fillMaxWidth().combinedClickable(
                onClick = { cardVM.click() },
                onLongClick = { cardVM.longClick() }
            ).padding(5.dp)
        ) {
            val (topLine, time, locationIcon, location) = createRefs()
            Row(Modifier.fillMaxWidth().constrainAs(topLine) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }, verticalAlignment = Alignment.CenterVertically) {
                Text(state.display, Modifier, maxLines = 1)
                Text(
                    state.courseTerm,
                    Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0x99ffaa00))
                        .padding(2.dp, 1.dp),
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
            Text(state.courseLocation?:"[教室不详]", Modifier.constrainAs(location) {
                top.linkTo(time.bottom)
                start.linkTo(locationIcon.end)
            }, fontSize = 14.sp)
        }

    }
}