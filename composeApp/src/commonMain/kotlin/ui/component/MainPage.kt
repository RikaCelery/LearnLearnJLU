package ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import ui.vm.AppVM

@Composable
fun BoxScope.MainPage(vm: AppVM) {
    var setStream: ((String) -> Unit)? by remember { mutableStateOf(null) }
    LazyColumn {
        items(vm.videos, key = { it.id }) { cardVM ->
            CourseCard(
                cardVM, {
                    vm.snack("下载请长按")
                }
            ) {
                setStream = it
                vm.requireSelectStream()
            }
        }
    }
    FloatingActionButton({
        vm.refreshVideos()
    }, Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
        Icon(Icons.Filled.Refresh, null)
    }
    val ui by vm.uiState.collectAsState()
    AnimatedVisibility(ui.showStreamsSelectPanel) {
        Dialog(onDismissRequest = { vm.dismissSelectStream() }, DialogProperties()) {
            Card() {
                ConstraintLayout(Modifier.padding(20.dp)){
                    val (btn1,btn2 )= createRefs()
                    Button({
                        requireNotNull(setStream)
                        setStream!!("HDMI")
                        vm.dismissSelectStream()
                    },Modifier.constrainAs(btn1){
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }) {
                        Text("电脑画面")
                    }
                    Button({
                        requireNotNull(setStream)
                        setStream!!("教师机位")
                        vm.dismissSelectStream()
                    },Modifier.constrainAs(btn2){
                        start.linkTo(parent.start)
                        top.linkTo(btn1.bottom,10.dp)
                        end.linkTo(parent.end)
                    }) {
                        Text("教师画面")
                    }
                }
            }
        }
    }
}