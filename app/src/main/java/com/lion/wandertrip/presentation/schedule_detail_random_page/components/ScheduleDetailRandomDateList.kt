package com.lion.wandertrip.presentation.schedule_detail_random_page.components



import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.maps.model.LatLng
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.util.ContentTypeId
import com.google.firebase.Timestamp
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.ScheduleDetailRandomViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.reorderable
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@Composable
fun ScheduleDetailRandomDateList(
    viewModel: ScheduleDetailRandomViewModel,
    tripSchedule: TripScheduleModel,
    formatTimestampToDate: (Timestamp) -> String
) {
    // 구글 맵 터치 감지
    var isMapTouched by remember { mutableStateOf(false) }

    // 재정렬 모드 상태 (초기엔 false)
    var isReorderMode by remember { mutableStateOf(false) }

    val tripScheduleItems by viewModel.tripScheduleItems.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        userScrollEnabled = !isMapTouched,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tripSchedule.scheduleDateList.forEachIndexed { index, date ->
            // 날짜 헤더 및 Google Map 표시
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "DAY ${index + 1}",
                            fontSize = 25.sp,
                            fontFamily = NanumSquareRound,
                        )
                        Text(
                            text = formatTimestampToDate(date),
                            fontSize = 14.sp,
                            fontFamily = NanumSquareRoundRegular
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(top = 5.dp)
                    ) {
                        // 해당 날짜의 스케줄 항목 필터링 후 지도에 전달
                        ScheduleDetailRandomGoogleMap(
                            scheduleItems = tripScheduleItems
                                .filter { it.itemDate.seconds == date.seconds }
                                .sortedBy { it.itemIndex },
                            viewModel = viewModel,
                            onTouch = { touched -> isMapTouched = touched },
                            mapScheduleDate = tripSchedule.scheduleDateList[index],
                            selectedLocation = LatLng(
                                tripSchedule.lat?.toDouble() ?: 0.0,
                                tripSchedule.lng?.toDouble() ?: 0.0
                            )
                        )
                    }
                }
            }

            // 해당 날짜 그룹의 일정 항목들을 재정렬할 수 있는 리스트 부분
            item {
                Log.d("ScheduleDetailDateList", "----------------------------------")
                tripScheduleItems.forEach {
                    Log.d("ScheduleDetailDateList", "${it.itemTitle} ${it.itemIndex}")
                }
                Log.d("ScheduleDetailDateList", "----------------------------------")
                // 날짜 그룹에 해당하는 항목 필터링
                val filteredItems = tripScheduleItems
                    .filter { it.itemDate.seconds == date.seconds }
                    .sortedBy { it.itemIndex }
                if (filteredItems.isNotEmpty()) {

                    // 재정렬 작업을 위한 임시 리스트(드래그 시 바로 viewModel에 반영하지 않음)
                    val tempList = remember { mutableStateListOf<ScheduleItem>() }
                    // filteredItems가 바뀔 때 tempList도 갱신
                    LaunchedEffect(filteredItems) {
                        tempList.clear()
                        tempList.addAll(filteredItems)
                    }

                    // 재정렬 관련 상태 (임시 리스트만 업데이트)
                    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
                        tempList.apply {
                            add(to.index, removeAt(from.index))
                        }
                    })

                    // 임시 리스트의 항목 높이 고정 (무한 높이 문제 해결)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 10000.dp)
                    ) {
                        LazyColumn(
                            state = reorderState.listState,
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isReorderMode)
                                        Modifier
                                            .reorderable(reorderState)  // detectReorderAfterLongPress 제거
                                    else Modifier
                                )
                        ) {
                            items(
                                items = tempList, // db에 있는 리스트가 아니고 tempList 기반으로 리스트 출력
                                key = { it.itemDocId }
                            ) { scheduleItem ->
                                ReorderableItem(reorderState, key = scheduleItem.itemDocId) {
                                    Row(
                                        modifier = Modifier
                                            .height(IntrinsicSize.Min)
                                            .clickable {
                                                viewModel.selectedLocation.value = LatLng(
                                                    scheduleItem.itemLatitude,
                                                    scheduleItem.itemLongitude
                                                )
                                                viewModel.selectedDate.value = scheduleItem.itemDate
                                            }
                                    ) {
                                        ScheduleDetailVerticalDividerWithCircle()
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 20.dp)
                                                .weight(1f)
                                        ) {
                                            Row {
                                                Text(
                                                    text = scheduleItem.itemTitle,
                                                    fontSize = 14.sp,
                                                    fontFamily = NanumSquareRoundRegular,
                                                    modifier = Modifier.padding(start = 10.dp)
                                                )
                                                Text(
                                                    text = scheduleItem.itemType,
                                                    fontSize = 10.sp,
                                                    fontFamily = NanumSquareRoundRegular,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier.padding(start = 10.dp)
                                                )
                                            }
                                            Row {
                                                scheduleItem.itemReviewImagesURL.forEach { imageUrl ->
                                                    val painter: AsyncImagePainter =
                                                        rememberAsyncImagePainter(model = imageUrl)

                                                    Box(
                                                        modifier = Modifier
                                                            .size(80.dp)
                                                            .padding(horizontal = 5.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                    ) {
                                                        // 첫번째 if: 로딩 상태일 때 Lottie 애니메이션을 표시
                                                        if (painter.state is AsyncImagePainter.State.Loading) {
                                                            val composition by rememberLottieComposition(
                                                                LottieCompositionSpec.RawRes(R.raw.lottie_shimmer)
                                                            )
                                                            LottieAnimation(
                                                                composition = composition,
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                        // 두번째 if: 로딩 상태일 때 새 painter2를 생성해 최신 상태를 반영하고, 이미지가 로드되면 이를 표시
                                                        if (painter.state is AsyncImagePainter.State.Loading) {
                                                            val painter2 =
                                                                rememberAsyncImagePainter(model = imageUrl)
                                                            Log.d(
                                                                "ScheduleDetailDateList3",
                                                                "imageUrl: $imageUrl"
                                                            )
                                                            Image(
                                                                painter = painter2,
                                                                contentDescription = "후기 이미지",
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    }

                                                }
                                            }
                                            if (scheduleItem.itemReviewText.isNotEmpty()) {
                                                Text(
                                                    text = scheduleItem.itemReviewText,
                                                    fontSize = 12.sp,
                                                    fontFamily = NanumSquareRoundRegular,
                                                    modifier = Modifier.padding(start = 10.dp)
                                                )
                                            }
                                        }
                                        // 메뉴 또는 드래그 핸들 아이콘 영역
                                        Box {
                                            if (isReorderMode) {
                                                // 재정렬 모드일 때는 드래그 핸들 아이콘만 표시
                                                Icon(
                                                    imageVector = Icons.Filled.Menu,
                                                    contentDescription = "드래그 핸들",
                                                    modifier = Modifier
                                                        .padding(top = 5.dp)
                                                        .size(18.dp)
                                                        .detectReorder(reorderState)
                                                )
                                            } else {
                                                // 일반 모드일 때는 더보기 아이콘 및 드롭다운 메뉴 표시
                                                var expanded by remember { mutableStateOf(false) }
                                                IconButton(
                                                    modifier = Modifier
                                                        .padding(top = 5.dp)
                                                        .size(18.dp),
                                                    onClick = { expanded = true }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.MoreVert,
                                                        contentDescription = "더보기"
                                                    )
                                                }
                                                ScheduleDetailDropDawn(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false },
                                                    onDelete = {
                                                        expanded = false
                                                        viewModel.removeTripScheduleItem(
                                                            viewModel.tripScheduleDocId,
                                                            scheduleItem.itemDocId,
                                                            scheduleItem.itemDate
                                                        )
                                                    },
                                                    onReview = {
                                                        expanded = false
                                                        viewModel.moveToScheduleItemReviewScreen(
                                                            viewModel.tripScheduleDocId,
                                                            scheduleItem.itemDocId,
                                                            scheduleItem.itemTitle,
                                                        )
                                                    },
                                                    onMove = {
                                                        // "위치 조정" 선택 시 재정렬 모드 활성화
                                                        isReorderMode = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 재정렬 모드일 때 "완료" 버튼 표시
                    if (isReorderMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {

                            OutlinedButton(
                                onClick = {
                                    // "취소" 버튼 클릭: 임시 리스트를 원래 순서(filteredItems)로 되돌리고 재정렬 모드 종료
                                    tempList.clear()
                                    tempList.addAll(filteredItems)
                                    isReorderMode = false
                                }
                            ) {
                                Text(
                                    text = "취소",
                                    fontSize = 12.sp,
                                    fontFamily = NanumSquareRound
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // 변경 완료 버튼
                            OutlinedButton(
                                onClick = {
                                    // 완료 버튼 클릭 시, 임시 리스트 기준으로 각 항목의 itemIndex를 1부터 업데이트
                                    viewModel.updateItemsOrderForDate(tempList, date)

                                    isReorderMode = false
                                }
                            ) {
                                Text(
                                    text = "완료",
                                    fontSize = 12.sp,
                                    fontFamily = NanumSquareRound
                                )
                            }
                        }
                    }
                }
            }

            // 각 날짜 그룹 하단에 '추가' 버튼들
            item {
                if (!isReorderMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.moveToScheduleRandomSelectItemScreen(
                                    ContentTypeId.TOURIST_ATTRACTION.contentTypeCode,
                                    date
                                )

                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "관광지 추가",
                                fontSize = 12.sp,
                                fontFamily = NanumSquareRound
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.moveToScheduleRandomSelectItemScreen(
                                    ContentTypeId.RESTAURANT.contentTypeCode,
                                    date
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "음식점 추가",
                                fontSize = 12.sp,
                                fontFamily = NanumSquareRound
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.moveToScheduleRandomSelectItemScreen(
                                    ContentTypeId.ACCOMMODATION.contentTypeCode,
                                    date
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "숙소 추가",
                                fontSize = 12.sp,
                                fontFamily = NanumSquareRound
                            )
                        }
                    }
                }
                CustomDividerComponent()
            }
        }
    }
}
