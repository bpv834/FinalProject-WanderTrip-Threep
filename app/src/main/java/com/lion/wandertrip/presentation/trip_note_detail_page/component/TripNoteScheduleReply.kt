package com.lion.wandertrip.presentation.trip_note_detail_page.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.wandertrip.model.TripNoteReplyModel
import com.lion.wandertrip.presentation.trip_note_detail_page.TripNoteDetailViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular

@Composable
fun TripNoteScheduleReply(
    tripNoteReply: TripNoteReplyModel,
    loginNickName : String,
    tripNoteDetailViewModel : TripNoteDetailViewModel,
    documentId : String
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // 작성자랑 로그인 유저가 동일하면 다이얼로그 띄움
                        if (tripNoteReply.userNickname == loginNickName) {
                            showDialog = true
                        }
                    }
                )
            }
    ) {

        // 닉네임 (작은 글씨)
        Text(
            text = tripNoteReply.userNickname,
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = NanumSquareRoundRegular,
            modifier = Modifier.padding(bottom = 1.dp)
        )

        // 댓글 내용
        Text(
            text = tripNoteReply.replyText,
            fontSize = 16.sp,
            fontFamily = NanumSquareRoundRegular,
            modifier = Modifier.padding(bottom = 3.dp)
        )

    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "댓글 삭제") },
            text = { Text(text = "이 댓글을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    // 삭제 메서드 호출
                    tripNoteDetailViewModel.deleteTripNoteReply(tripNoteReply.tripNoteReplyDocId)
                    tripNoteDetailViewModel.gettingTripNoteReplyData(tripNoteDocumentId = documentId)
                    showDialog = false
                }) {
                    Text(text = "삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "취소")
                }
            }
        )
    }



}