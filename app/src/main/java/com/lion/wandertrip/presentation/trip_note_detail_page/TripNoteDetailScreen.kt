package com.lion.wandertrip.presentation.trip_note_detail_page

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.lion.wandertrip.R
import com.lion.wandertrip.presentation.trip_note_detail_page.component.TripNoteScheduleList
import com.lion.wandertrip.presentation.trip_note_detail_page.component.TripNoteScheduleReply
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.bumptech.glide.integration.compose.GlideImage


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun TripNoteDetailScreen(
    tripNoteDocumentId: String,
    tripNoteDetailViewModel: TripNoteDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect (Unit){
        // 여행기 상세 데이터 가져오기
        tripNoteDetailViewModel.gettingTripNoteDetailData(tripNoteDocumentId)
    }

    LaunchedEffect (tripNoteDetailViewModel.tripNoteDetailList.size){

        // 댓글 리스트 가져오기
        tripNoteDetailViewModel.gettingTripNoteReplyData(tripNoteDocumentId)

    }

    // 휴지통 다이얼로그 상태값
    var showDeleteDialogState by remember { mutableStateOf(false) }

    val images = tripNoteDetailViewModel.tripNoteModelValue.value.tripNoteImage
    // HorizontalPager에 필요한 상태를 pageCount를 전달하여 초기화합니다.
    val pagerState = rememberPagerState(pageCount = { images.size })

    // 댓글 작성칸 (네모박스 형태)
    var commentText by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }


    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White, // 배경색을 흰색으로 설정
                titleContentColor = Color.Black // 제목 색상을 검은색으로 설정 (가독성)
            ),
                navigationIcon = {
                    IconButton(onClick = { tripNoteDetailViewModel.navigationButtonClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = "",
                        fontSize = 20.sp,
                        fontFamily = NanumSquareRound // 폰트 설정 (옵션)
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp), // 아이콘 간 간격을 0으로 설정
                        // verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 본인이 작성한 여행기일떄만 아이콘 뜨게
                        if(tripNoteDetailViewModel.showTopAppBarDeleteMenuState.value) {
                            // 휴지통 아이콘 (오른쪽 상단에 배치)
                            IconButton(
                                onClick = {
                                    showDeleteDialogState = true },
                                modifier = Modifier.padding(start = 20.dp)
                            ) {
                                Icon(
                                    // imageVector = Icons.Filled.Delete,
                                    painter = painterResource(id = R.drawable.ic_delete_24px),
                                    contentDescription = "Delete"
                                )
                            }
                        }


                        // 삭제 확인 다이얼로그
                        if (showDeleteDialogState) {
                            AlertDialog(
                                onDismissRequest = {
                                    // 다이얼로그 닫기
                                    showDeleteDialogState = false
                                },
                                title = {
                                    Text(text = "삭제 확인")
                                },
                                text = {
                                    Text(text = "정말로 삭제하시겠습니까?")
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        // 삭제 버튼 클릭
                                        tripNoteDetailViewModel.deleteButtonClick(tripNoteDocumentId)
                                        // 다이얼로그 닫기
                                        showDeleteDialogState = false
                                    }) {
                                        Text(text = "확인")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        // 다이얼로그 닫기
                                        showDeleteDialogState = false
                                    }) {
                                        Text(text = "취소")
                                    }
                                }
                            )
                        }


                        // 다운로드 아이콘 (오른쪽 상단에 배치)
                        IconButton(
                            onClick = { tripNoteDetailViewModel.bringTripNote(tripNoteDetailViewModel.textFieldTripNoteScheduleDocId, tripNoteDocumentId) },
                            modifier = Modifier.padding(end = 3.dp)
                        ) {
                            Icon(
                                // imageVector = Icons.Filled.Download,
                                painter = painterResource(id = R.drawable.ic_download_24px),
                                contentDescription = "Download"
                            )
                        }
                    }
                }
            )
        }
    )
        {
            LazyColumn(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
            ) {

                // 이미지 슬라이더
                if (tripNoteDetailViewModel.tripNoteModelValue.value.tripNoteImage.isNotEmpty()) {
                    item {
                        var showFullImage by remember { mutableStateOf(false) }  // 전체 이미지 보기 상태
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.padding(top = 10.dp)
                                .fillMaxWidth()
                                .height(200.dp)
                        ) { page ->

                            val imageUrl = images[page]

                            // GlideImage 클릭 시 showFullImage 상태를 true로 변경
                            GlideImage(
                                model = imageUrl,
                                contentDescription = "Image",
                                modifier = Modifier
                                    .fillMaxWidth() // 가로 꽉 차게
                                    .height(200.dp) // 세로 고정
                                    .clickable { showFullImage = true },
                                contentScale = ContentScale.Crop // 이미지 자르기
                            )

                            // 클릭 시 전체 이미지 보여주는 Dialog
                            if (showFullImage) {
                                // 다이얼로그에 이미지와 닫기 버튼 추가
                                Dialog(onDismissRequest = { showFullImage = false }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(0.dp)
                                            .pointerInput(Unit) {
                                                // 화면의 아무 곳이나 클릭하면 다이얼로그를 닫음
                                                detectTapGestures(onTap = { showFullImage = false })
                                            }
                                    ) {
                                        // 전체 화면 이미지 표시
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Full Image",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(0.dp),
                                            contentScale = ContentScale.Fit // 이미지 비율 유지
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 여행기 제목
                    item {
                        Text(
                            text = tripNoteDetailViewModel.textFieldTripNoteSubject.value,
                            fontSize = 27.sp,
                            fontFamily = NanumSquareRound,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 여행기 내용
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tripNoteDetailViewModel.textFieldTripNoteContent.value,
                            fontSize = 15.sp,
                            fontFamily = NanumSquareRoundRegular
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 사용자 닉네임과 아이콘 버튼
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = tripNoteDetailViewModel.textFieldTripNoteNickName.value ?: "닉네임 없음",
                            fontSize = 13.sp,
                            fontFamily = NanumSquareRound,
                            modifier = Modifier.padding(end = 3.dp)
                                .clickable { tripNoteDetailViewModel.clickNickname() }
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_forward_ios_24px),
                            contentDescription = "Next",
                            modifier = Modifier
                                .size(11.dp) // 아이콘 크기
                                .clickable { tripNoteDetailViewModel.clickNickname() } // 클릭 이벤트
                        )


                        Spacer(modifier = Modifier.weight(1f))

                        // 다운로드 아이콘 (오른쪽 상단에 배치)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_download_24px),
                            contentDescription = "Download",
                            modifier = Modifier.size(20.dp)
                                .padding(end = 3.dp)
                        )

                        Text(
                            text = tripNoteDetailViewModel.textFieldTripNoteScrap.value ?: "0",
                            fontSize = 15.sp,
                            fontFamily = NanumSquareRound,
                            modifier = Modifier.padding(end = 13.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }

                // 가로선
                item {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 11.dp), // 좌우 여백 설정
                        thickness = 1.dp // 선의 두께
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(11.dp))
                }

                // 여행 일정 리스트
                item {
                    TripNoteScheduleList(
                        viewModel = tripNoteDetailViewModel,
                        tripSchedule = tripNoteDetailViewModel.tripSchedule.value,
                        formatTimestampToDate = { timestamp -> tripNoteDetailViewModel.formatTimestampToDate(timestamp) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                // 댓글 작성소개
                item {
                    Text(
                        text = "댓글",
                        fontSize = 25.sp,
                        fontFamily = NanumSquareRound,
                        modifier = Modifier.padding(bottom = 11.dp, start = 15.dp)
                    )
                }

                items(tripNoteDetailViewModel.tripNoteReplyList) { tripNoteReply ->
                    TripNoteScheduleReply(
                        tripNoteReply = tripNoteReply,
                        loginNickName = tripNoteDetailViewModel.nickName,
                        tripNoteDetailViewModel = tripNoteDetailViewModel,
                        documentId = tripNoteDocumentId
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                }


                // 댓글 작성란
                item {
                    TextField(
                        value = tripNoteDetailViewModel.textFieldTripNoteReply.value,
                        onValueChange = { tripNoteDetailViewModel.textFieldTripNoteReply.value = it },
                        placeholder = {
                            if (tripNoteDetailViewModel.textFieldTripNoteReply.value.isEmpty()) {
                                Text("댓글을 입력하세요",
                                    color = Color.Gray) // 초기 플로팅 텍스트로 사용
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isFocused) 62.dp else 54.dp)
                             .padding(start = 11.dp, end = 11.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused }
                            .border(0.dp, Color.Transparent)
                            .background(Color.LightGray, shape = RoundedCornerShape(3.dp)), // 배경색만 회색으로,
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontFamily = NanumSquareRoundRegular
                        ),
                        maxLines = 5, // 최대 줄 수
                        singleLine = false, // 여러 줄 입력 가능
                        shape = RoundedCornerShape(4.dp), // 둥근 모서리
                        trailingIcon = {
                            if (tripNoteDetailViewModel.textFieldTripNoteReply.value.isNotEmpty()) {
                                IconButton(
                                    onClick =
                                    {   // 제출하기
                                        tripNoteDetailViewModel.addReplyClick(tripNoteDetailViewModel.tripNoteModelValue.value.tripNoteDocumentId,tripNoteDocumentId)
                                        tripNoteDetailViewModel.gettingTripNoteReplyData(tripNoteDocumentId)
                                        // 제출하면 필드 비어있게
                                        tripNoteDetailViewModel.textFieldTripNoteReply.value = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Clear Comment"
                                    )
                                }
                            }
                        }
                    )
                }


                item {
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

//            // ✅ 로딩 화면 추가 (투명 오버레이)
//            if (isLoading) {
//                LottieLoadingIndicator() // ✅ 로딩 애니메이션
//            }

        }
}