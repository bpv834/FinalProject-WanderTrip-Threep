package com.lion.wandertrip.presentation.trip_note_select_down_page

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomAlertDialog
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.component.BlueButton
import com.lion.wandertrip.presentation.trip_note_select_down_page.component.TripNoteSelectDownItemList



@Composable
fun TripNoteSelectDownScreen(
    tripNoteScheduleDocId: String,
    documentId : String,
    tripNoteSelectDownViewModel: TripNoteSelectDownViewModel = hiltViewModel())
{
    Log.d("TripNoteSelectDownScreen","TripNoteSelectDownScreen!!!")
    tripNoteSelectDownViewModel.gettingTripNoteDetailData()

    // 다이얼로그를 보여주는 상태 - 완료
    val showDialogState = tripNoteSelectDownViewModel.showDialogState
    // 다이얼로그를 보여주는 상태 - 새 여행
    val showDialogStateNew = tripNoteSelectDownViewModel.showDialogStateNew
    // 다이얼로그를 보여주는 상태 - 일정 담기할때 일정 안누름
    val showDialogNotState = tripNoteSelectDownViewModel.showDialogNotState

    val scheduleDocId = tripNoteSelectDownViewModel.scheduleDocId.value

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CustomTopAppBar(
                title = "",
                navigationIconImage = Icons.AutoMirrored.Filled.ArrowBack,
                navigationIconOnClick = {
                    tripNoteSelectDownViewModel.navigationButtonClick()
                },
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp)) // 상단 여백


            Text(
                text = "일정을 담을 여행을",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "선택해주세요!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(17.dp)) // 상단 여백


            // 새 여행 만들어 담기 버튼
            BlueButton(text = "새 여행에 담기",
                buttonWidth = 150.dp) {
                tripNoteSelectDownViewModel.selectNewButtonClick()
            }

            // 다이얼로그 표시
            if (showDialogStateNew.value) {
                CustomAlertDialog(
                    showDialogState = showDialogStateNew,
                    title = "일정 담기",
                    text = "새 일정에 그대로 담을까요?",
                    confirmButtonTitle = "확인",
                    dismissButtonTitle = "취소",
                    confirmButtonOnClick = {
                        // 확인 버튼 클릭 시 동작
                        tripNoteSelectDownViewModel.onConfirmNewClick()
                        // 일정 제목 입력 화면으로 이동
                        tripNoteSelectDownViewModel.goScheduleTitleButtonClick(tripNoteScheduleDocId, documentId)

                    },
                    dismissButtonOnClick = {
                        // 취소 버튼 클릭 시 동작
                        tripNoteSelectDownViewModel.onDismissNewClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp)) // 하단 여백

            Text(
                text = "나의 다가오는 여행",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp)) // 하단 여백

            // 스크롤 가능한 리스트 부분
            Box(modifier = Modifier.weight(1f)) {
                TripNoteSelectDownItemList(
                    dataList = tripNoteSelectDownViewModel.tripNoteMyScheduleList,
                    viewModel = tripNoteSelectDownViewModel,
                    onRowClick = {tripSchedule ->
                        tripNoteSelectDownViewModel.gettingSelectId(tripSchedule.tripScheduleDocId)
                    },
                )
            }

            Spacer(modifier = Modifier.height(13.dp)) // 하단 여백

            // 완료 버튼
            BlueButton(text = "완료",) {
                if(scheduleDocId.isEmpty()){
                    tripNoteSelectDownViewModel.showDialogNotState.value = true
                }
                else {
                    tripNoteSelectDownViewModel.showDialogState.value = true
                }
            }

            // 다이얼로그 표시
            if (tripNoteSelectDownViewModel.showDialogNotState.value) {
                CustomAlertDialog(
                    showDialogState = showDialogNotState,
                    title = "일정 선택",
                    text = "일정을 선택해주세요.",
                    confirmButtonTitle = "확인",
                    dismissButtonTitle = "취소",
                    confirmButtonOnClick = {
                        tripNoteSelectDownViewModel.onConfirmNotClick()
                    },
                    dismissButtonOnClick = {
                        tripNoteSelectDownViewModel.onDismissNotClick()
                    }
                )
            }

            // 다이얼로그 표시
            if (tripNoteSelectDownViewModel.showDialogState.value) {
                CustomAlertDialog(
                    showDialogState = showDialogState,
                    title = "일정 선택",
                    text = "이 일정을 그대로 담을까요?",
                    confirmButtonTitle = "확인",
                    dismissButtonTitle = "취소",
                    confirmButtonOnClick = {
                        // 확인 버튼 클릭 시 동작
                        tripNoteSelectDownViewModel.onConfirmClick()
                        // 일정 상세 입력 화면으로 이동
                        tripNoteSelectDownViewModel.selectFinishButtonClick(
                            tripNoteScheduleDocId,
                            scheduleDocId,
                            documentId
                        )

                    },
                    dismissButtonOnClick = {
                        // 취소 버튼 클릭 시 동작
                        tripNoteSelectDownViewModel.onDismissClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp)) // 하단 여백
        }
    }
}
