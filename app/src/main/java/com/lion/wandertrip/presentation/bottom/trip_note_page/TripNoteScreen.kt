package com.lion.wandertrip.presentation.bottom.trip_note_page

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.presentation.bottom.schedule_page.component.ScheduleIconButton
import com.lion.wandertrip.ui.theme.Gray0
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import kotlinx.coroutines.launch

@Composable
fun TripNoteScreen(
    tripNoteViewModel: TripNoteViewModel = hiltViewModel()
) {

    // 리사이클러뷰 항목 데이터 초기화
    tripNoteViewModel.gettingTripNoteData()

    tripNoteViewModel.topAppBarTitle.value = "여행기 모아보기"



    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { tripNoteViewModel.addButtonOnClick() },
                modifier = Modifier.padding(bottom = 0.dp).
                padding(top = 160.dp)
                    .absoluteOffset(y = 50.dp),
                content = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "+")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxSize()
        ) {
            // 최상단 중앙에 텍스트 배치
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = tripNoteViewModel.topAppBarTitle.value,
                    fontFamily = NanumSquareRound,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .weight(1f, fill = false) // 가중치 설정
                )
            }

//            // 리사이클러뷰
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 10.dp)
//            ) {
//                Log.d("TripNoteScreen", "ImageUrisMap: ${tripNoteViewModel.imageUrisMap}")
//                itemsIndexed(tripNoteViewModel.tripNoteList) { index,tripNote ->
//                    // 로그로 각 인덱스의 imageUris 확인
//                    Log.d("TripNoteScreen", "Index: $index, imageUris: ${tripNoteViewModel.imageUrisMap[index]}")
//
//                    TripNoteItem(
//                        tripNote = tripNote,
//                        onClick = { tripNoteViewModel.listItemOnClick(tripNote.tripNoteDocumentId) },
//                        modifier = Modifier
//                            .fillMaxWidth() // 항목이 화면 너비를 가득 차게
//                            .padding(vertical = 8.dp) // 항목 사이에 간격 추가
//                            .background(
//                                color = Color.White, // 배경색
//                                shape = RoundedCornerShape(12.dp) // 둥근 모서리
//                            )
//                            .padding(0.dp),
//                        //imageUris = tripNoteViewModel.imageUrisMap[index]?: emptyList()
//                        imageUris = tripNoteViewModel.imageUrisMap[index]?.filterNotNull() ?: emptyList()
//
//                    )
//                }
//            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                Log.d("TripNoteScreen", "ImageUrisMap: ${tripNoteViewModel.imageUrisMap}")

                itemsIndexed(tripNoteViewModel.tripNoteList) { index, tripNote ->
                    // 로그로 각 인덱스의 imageUris 확인
                    Log.d("TripNoteScreen", "Index: $index, imageUris: ${tripNoteViewModel.imageUrisMap[index]}")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp), // 항목 사이 간격 추가
                        shape = RoundedCornerShape(14.dp), // 모서리 둥글게
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 카드의 그림자 설정
                        colors = CardDefaults.cardColors(Gray0)
                    ) {
                        TripNoteItem(
                            tripNote = tripNote,
                            onClick = { tripNoteViewModel.listItemOnClick(tripNote.tripNoteDocumentId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, shape = RoundedCornerShape(12.dp))
                                .padding(0.dp),
                            imageUris = tripNoteViewModel.imageUrisMap[index]?.filterNotNull() ?: emptyList()
                        )
                    }
                }
            }


        }
    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TripNoteItem(tripNote: TripNoteModel,
                 onClick: () -> Unit,
                 modifier : Modifier,
                 imageUris: List<Uri?>) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        // 제목 (굵은 글씨)
        Text(
            text = tripNote.tripNoteTitle,
            fontSize = 20.sp,
            fontFamily = NanumSquareRound,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 닉네임 (작은 글씨)
        Text(
            text = "${tripNote.userNickname} 님의 여행기",
            fontSize = 14.sp,
            color = Color.Gray,
            fontFamily = NanumSquareRound,
            modifier = Modifier.padding(bottom = 15.dp, start = 1.dp)
        )


        // 이미지 리스트
        if (imageUris.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .wrapContentWidth(Alignment.Start) // Row를 왼쪽 정렬로 설정
                    .padding(bottom = 11.dp),
                 horizontalArrangement = Arrangement.spacedBy(3.dp) // 이미지 간 간격 유지

            ) {

                imageUris.forEach { imageUrl ->
                    Log.d("TripNoteItem", "ImageUri: $imageUrl")
                    imageUrl?.let {
                        GlideImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .height(200.dp) // 이미지 높이를 고정
                                .weight(1f) // 화면 너비를 균등하게 나누도록 설정
                                .clip(RoundedCornerShape(0.dp)) // 둥근 모서리 적용
                                .fillMaxWidth() // 너비를 꽉 채우기
                                .aspectRatio(1f) // 비율을 1:1로 맞추기
                                .then(Modifier.fillMaxHeight()),
                            contentScale = ContentScale.Crop// 높이를 고정
                        )
                    }
                }
            }
        }

        // 내용
        Text(
            text = tripNote.tripNoteContent,
            fontSize = 16.sp,
            fontFamily = NanumSquareRoundRegular,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

