package com.lion.wandertrip.presentation.rotate_map_page.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


@Composable
fun TravelConfirmDialog(
    onYesClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDismiss: () -> Unit,
    area:String
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "WanderTrip") },
        text = { Text("${area}로 떠나볼까요?") },
        confirmButton = {
            TextButton(onClick = onYesClick) {
                Text("여행가기")
            }
        },
        dismissButton = {
            TextButton(onClick = onRetryClick) {
                Text("취소")
            }
        }
    )
}

