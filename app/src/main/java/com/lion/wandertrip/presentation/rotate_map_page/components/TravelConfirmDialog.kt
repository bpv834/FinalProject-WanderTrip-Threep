package com.lion.wandertrip.presentation.rotate_map_page.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


@Composable
fun TravelConfirmDialog(
    onYesClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "WanderTrip") },
        confirmButton = {
            TextButton(onClick = onYesClick) {
                Text("YES")
            }
        },
        dismissButton = {
            TextButton(onClick = onRetryClick) {
                Text("RETRY")
            }
        }
    )
}

