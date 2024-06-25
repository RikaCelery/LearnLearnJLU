package ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ui.vm.AppUI
import ui.vm.AppVM

@Composable
fun PopUpDialog(state: AppUI, vm: AppVM) {
    AnimatedVisibility(state.isLoginInProgress) {
        Dialog(onDismissRequest = { }) {
            Card(Modifier.fillMaxHeight(.7f).fillMaxWidth()) {
                LinearProgressIndicator(
                    Modifier.fillMaxWidth().padding(top = 0.dp, bottom = 10.dp).align(Alignment.CenterHorizontally)
                )
                for (log in vm.loginLog) {
                    Text(
                        log,
                        Modifier.padding(bottom = 2.dp, start = 10.dp, end = 10.dp),
                        fontSize = 13.sp,
                        lineHeight = 13.sp
                    )
                }
            }

        }
    }
    AnimatedVisibility(state.isLoading) {
        Dialog(onDismissRequest = { }) {
            Card {
                CircularProgressIndicator(
                    Modifier.size(200.dp).padding(30.dp).align(Alignment.CenterHorizontally)
                )
            }

        }
    }
}