package com.lion.wandertrip.presentation.trip_note_detail_page.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.google.firebase.Timestamp
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.presentation.trip_note_detail_page.TripNoteDetailViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.util.ContentTypeId


@Composable
fun TripNoteScheduleList(
    viewModel: TripNoteDetailViewModel,
    tripSchedule: TripScheduleModel,
    formatTimestampToDate: (Timestamp) -> String,
    modifier: Modifier = Modifier
) {

    // 구글 맵 터치 감지
    var isMapTouched by remember { mutableStateOf(false) }

    // Column을 사용하여 전체 내용을 감싸고 스크롤을 직접 처리
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // 날짜 및 스케줄 내용 표시
        tripSchedule.scheduleDateList.forEachIndexed { index, date ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                // 날짜 표시
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .alignByBaseline(),
                        text = "DAY ${index + 1}",
                        fontSize = 25.sp,
                        fontFamily = NanumSquareRound,
                    )
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = formatTimestampToDate(tripSchedule.scheduleDateList[index]),
                        fontSize = 14.sp,
                        fontFamily = NanumSquareRoundRegular
                    )
                }

                // 해당 날짜의 스케줄 아이템 목록 필터링 후 정렬
                val filteredItems = viewModel.tripScheduleItems
                    .filter { it.itemDate.seconds == tripSchedule.scheduleDateList[index].seconds }
                    .sortedBy { it.itemIndex }

                // Google Map 표시 (해당 날짜의 스케줄 아이템 목록을 전달)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 5.dp),
                ) {
                    TripNoteScheduleGoogleMap(
                        scheduleItems = filteredItems,
                    )
                }

                // 상세 일정 리스트 표시
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp) // 요소들 사이에 간격 0
                ) {
                    filteredItems.forEach { scheduleItem ->
                        // 각 scheduleItem마다 개별적으로 expanded 상태를 선언
                        var expanded by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min) // intrinsic measurements
                        ) {
                            TripNoteScheduleDivider()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                                    .weight(1f)
                            ) {
                                Row {
                                    // 일정 항목 이름
                                    Text(
                                        text = scheduleItem.itemTitle,
                                        fontSize = 14.sp,
                                        fontFamily = NanumSquareRoundRegular,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )

                                    // 일정 항목 종류
                                    Text(
                                        text = scheduleItem.itemType,
                                        fontSize = 10.sp,
                                        fontFamily = NanumSquareRoundRegular,
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )
                                }

                                if (scheduleItem.itemReviewImagesURL.isNotEmpty()) {
                                    Row {
                                        scheduleItem.itemReviewImagesURL.forEach { imageUrl ->
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = "Review Image",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(90.dp)
                                                    .padding(start = 5.dp, end = 2.dp, bottom = 3.dp, top = 3.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            )
                                        }
                                    }
                                }

                                if (scheduleItem.itemReviewText != "") {
                                    // 후기
                                    Text(
                                        text = scheduleItem.itemReviewText,
                                        fontSize = 12.sp,
                                        fontFamily = NanumSquareRoundRegular,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )
                                }

                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))


                    // 가로선 추가
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp), // 좌우 여백 설정
                            thickness = 1.dp // 선의 두께
                        )

                }
            }
        }
    }
}
