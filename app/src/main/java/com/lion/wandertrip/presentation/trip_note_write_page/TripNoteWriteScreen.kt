package com.lion.wandertrip.presentation.trip_note_write_page

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lion.a02_boardcloneproject.component.CustomOutlinedTextField
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.a02_boardcloneproject.component.LikeLionOutlinedTextFieldEndIconMode
import com.lion.wandertrip.component.BlueButton
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.trip_note_write_page.component.AddImageButton
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.util.Tools

@Composable
fun TripNoteWriteScreen(
    scheduleDocId :String,
    tripNoteWriteViewModel: TripNoteWriteViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val schedule by tripNoteWriteViewModel.pickedSchedule.collectAsState()
    LaunchedEffect (tripNoteWriteViewModel.scheduleDocId){
        if(tripNoteWriteViewModel.scheduleDocId!="") // none은 기본값 처음 작성화면 열때
        tripNoteWriteViewModel.gettingSchedule()
    }

    // 앨범용 런처
    val albumLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        Tools.takeAlbumDataList(context, it, tripNoteWriteViewModel.tripNotePreviewBitmap)
        tripNoteWriteViewModel.isImagePicked.value=true
    }
    // 에러 상태 추가
    val scheduleTitleError = remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CustomTopAppBar(
                title = "여행기 작성",
                navigationIconImage = Icons.AutoMirrored.Filled.ArrowBack,
                navigationIconOnClick = {
                    tripNoteWriteViewModel.navigationButtonClick()
                }
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(it)
                .padding(start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.padding(top = 20.dp))

            Row(){
                Text(
                    text = "선택 일정 >",
                    fontFamily = NanumSquareRound,
                    fontSize = 17.sp,
                    color = Color.Gray, // 클릭 가능한 느낌을 주기 위해 색상 변경
                    modifier = Modifier
                        .clickable {
                            tripNoteWriteViewModel.addTripScheduleClick()
                        }
                        .padding(start = 0.dp) // 텍스트 간 간격 추가
                )

                Text(
                    text = schedule.scheduleTitle,
                    fontFamily = NanumSquareRound,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 11.dp, top = 0.dp)
                )
            }

            // 에러 메시지 표시
            if (scheduleTitleError.value.isNotEmpty()) {
                Text(
                    text = scheduleTitleError.value,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 0.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.padding(top = 36.dp))

            Text(
                text = "사진 선택",
                fontFamily = NanumSquareRound,
                fontSize = 23.sp,
                modifier = Modifier.padding(end = 5.dp)

            )

            Spacer(modifier = Modifier.padding(top = 20.dp))


            // 사진 추가하기
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 추가된 이미지를 왼쪽에 나열
                tripNoteWriteViewModel.tripNotePreviewBitmap.forEachIndexed { index, bitmap ->
                    Box(
                        modifier = Modifier
                            .size(73.dp) // 이미지 크기
                            .padding(end = 10.dp)
                            .clip(shape = RoundedCornerShape(12.dp))
                    ) {
                        // Bitmap을 ImageBitmap으로 변환
                        val imageBitmap = bitmap?.asImageBitmap()

                        // 이미지 표시
                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )

                            // X 버튼 (이미지 삭제)
                            IconButton(
                                onClick = {
                                    if (bitmap != null) {
                                        tripNoteWriteViewModel.removeTripNoteImage(bitmap)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd) // 이미지 위에 X 버튼 배치
                                    .size(24.dp) // X 버튼 크기 조정
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Image")
                            }
                        }
                    }
                }

                // 사진 추가 버튼 (2개 이하일 때만 활성화)
                if (tripNoteWriteViewModel.tripNotePreviewBitmap.size < 3) {
                    AddImageButton(
                        onClick = {
                            val pickVisualMediaRequest =
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            albumLauncher.launch(pickVisualMediaRequest)
                        },
                        modifier = Modifier.size(73.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 35.dp))


            // 여행기 제목 입력 요소
            CustomOutlinedTextField(
                textFieldValue = tripNoteWriteViewModel.tripNoteTitle,
                label = "여행기 제목",
                placeHolder = "제목을 입력해 주세요",
                trailingIconMode = LikeLionOutlinedTextFieldEndIconMode.TEXT,
                singleLine = true,
                isError = tripNoteWriteViewModel.tripNoteTitleIsError,  // 에러 상태 전달
                supportText = tripNoteWriteViewModel.tripNoteTitleError,  // 에러 메시지 전달
            )

            Spacer(modifier = Modifier.padding(top = 13.dp))


            // 여행기 내용 입력하기 텍스트 필드
            CustomOutlinedTextField(
                textFieldValue = tripNoteWriteViewModel.tripNoteContent,
                label = "후기",
                placeHolder = "여행기 후기를 입력해 주세요",
                singleLine = false,
                isError = tripNoteWriteViewModel.tripNoteContentIsError,  // 에러 상태 전달
                supportText = tripNoteWriteViewModel.tripNoteContentError,  // 에러 메시지 전달
                minLines = 15
            )


            // Spacer(modifier = Modifier.padding(top = 20.dp))

            Spacer(modifier = Modifier.weight(1f))

            // 게시 버튼
            BlueButton(
                text = "게시하기",
                paddingTop = 5.dp,
                onClick = {
                    // 제목이 비어있는지 확인
                    if (tripNoteWriteViewModel.tripNoteTitle.value.isEmpty()) {
                        // 제목 에러 처리
                        tripNoteWriteViewModel.tripNoteTitleError.value = "제목을 입력해 주세요."
                        tripNoteWriteViewModel.tripNoteTitleIsError.value = true
                    } else {
                        // 제목 에러 메시지 초기화
                        tripNoteWriteViewModel.tripNoteTitleError.value = ""
                        tripNoteWriteViewModel.tripNoteTitleIsError.value = false
                    }

                    // 내용이 비어있는지 확인
                    if (tripNoteWriteViewModel.tripNoteContent.value.isEmpty()) {
                        // 내용 에러 처리
                        tripNoteWriteViewModel.tripNoteContentError.value = "내용을 입력해 주세요."
                        tripNoteWriteViewModel.tripNoteContentIsError.value = true
                    } else {
                        // 내용 에러 메시지 초기화
                        tripNoteWriteViewModel.tripNoteContentError.value = ""
                        tripNoteWriteViewModel.tripNoteContentIsError.value = false
                    }

                    // 일정 선택 확인
                    if (schedule.scheduleTitle == "") {
                        scheduleTitleError.value = "일정을 선택해 주세요."
                    } else {
                        scheduleTitleError.value = ""
                    }

                    // 제목과 내용이 모두 비어 있지 않으면 tripNoteDoneClick 호출
                    if (tripNoteWriteViewModel.tripNoteTitle.value.isNotEmpty() && tripNoteWriteViewModel.tripNoteContent.value.isNotEmpty() && schedule.scheduleTitle!="" ){
                        tripNoteWriteViewModel.tripNoteDoneClick()
                    }
                }
            )

            Spacer(modifier = Modifier.padding(top = 28.dp))

        }

        // 프로그레스 비행기
        if (tripNoteWriteViewModel.isProgressVisible.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // 화면 전체를 덮음
                    .clickable(enabled = false) {} // 클릭 방지
                    .padding(13.dp),
                contentAlignment = Alignment.Center // 내용 중앙 정렬
            ) {
                LottieLoadingIndicator(
                )
            }
        }


        Spacer(modifier = Modifier.padding(top = 38.dp))
    }
}
