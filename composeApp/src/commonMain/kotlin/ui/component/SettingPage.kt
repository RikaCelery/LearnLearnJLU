package ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ui.vm.AppUI
import ui.vm.AppVM

@Composable
fun SettingPage(state: AppUI, vm: AppVM) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(Modifier) {
            TextField(state.username, { vm.setUsername(it) }, Modifier.fillMaxWidth(.5f), label = {
                Text("用户名")
            })
            TextField(state.password, { vm.setPassword(it) }, Modifier.fillMaxWidth(1f), label = {
                Text("密码")
            }, visualTransformation = PasswordVisualTransformation())
        }
        Button({
            vm.casLogin {
                vm.refreshTerms {
                    vm.refreshVideos()
                }
            }
        }) {
            Text("登录")
        }
    }
}