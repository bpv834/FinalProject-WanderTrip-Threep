package com.lion.wandertrip.presentation.rotate_map_page.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NoAttractionDialog(
    onYesClick: () -> Unit,
    onNoClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("해당 지역에 관광지가 없습니다.") },
        text = { Text("그래도 여행하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onYesClick) {
                Text("YES")
            }
        },
        dismissButton = {
            TextButton(onClick = onNoClick) {
                Text("NO")
            }
        }
    )
}

