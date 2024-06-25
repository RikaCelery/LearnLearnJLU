package ui.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.vm.AppVM

@Composable
fun BoxScope.MainPage(vm: AppVM) {
    LazyColumn {
        items(vm.videos, key = { it.id }) {
            CourseCard(
                it
            )
        }
    }
    FloatingActionButton({
        vm.refreshVideos()
    }, Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
        Icon(Icons.Filled.Refresh, null)
    }
}