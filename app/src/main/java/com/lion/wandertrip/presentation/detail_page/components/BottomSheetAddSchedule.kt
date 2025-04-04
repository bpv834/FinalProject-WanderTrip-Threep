package com.lion.wandertrip.presentation.detail_page.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.lion.a02_boardcloneproject.component.CustomIconButton
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.presentation.detail_page.DetailViewModel
import com.lion.wandertrip.ui.theme.wanderBlueColor
import com.lion.wandertrip.util.CustomFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetAddSchedule(
    detailViewModel: DetailViewModel
) {
    LaunchedEffect(Unit) {
        detailViewModel.tripScheduleList.clear()
        detailViewModel.getTripSchedule()
    }
    val schedules = detailViewModel.tripScheduleList
    // 시트 사이즈 기억해서 초기화 되지않게 하는 것
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        // 바텀시트가 닫힐때마다 항상 동작하는 부분
        onDismissRequest = {
            detailViewModel.isAddScheduleSheetOpen.value = false
            // 시트가 닫힐때 추가 아이콘을 숨긴다.
            detailViewModel.isShownAddScheduleIconValue.value = false
        },
        sheetState = bottomSheetState // 바텀시트 상태 적용
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "내 여행",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (detailViewModel.isShownAddScheduleIconValue.value)
                        CustomIconButton(
                            // 일정 추가버튼
                            icon = ImageVector.vectorResource(R.drawable.ic_calendar_add_on_24px),
                            iconButtonOnClick = {
                                // 클릭 인덱스
                                detailViewModel.addSchedule(
                                    title = detailViewModel.tripCommonContentModelValue.value.title?:"이름없음",
                                    tripScheduleDocId = schedules[detailViewModel.truedIdx.value].tripScheduleDocId,
                                    contentId = detailViewModel.tripCommonContentModelValue.value.contentId?:"",
                                    date = schedules[detailViewModel.truedIdx.value].scheduleDateList[detailViewModel.scheduleDatePickerTruedIdx.value],
                                    type =detailViewModel.tripCommonContentModelValue.value.contentTypeId.toString(),
                                    lat =detailViewModel.tripCommonContentModelValue.value.mapLat?.toDouble()?:0.0,
                                    lng = detailViewModel.tripCommonContentModelValue.value.mapLng?.toDouble()?:0.0,
                                )
                            })
                }
            }

            items(schedules) { schedule ->
                ScheduleItem(
                    detailViewModel = detailViewModel,
                    schedule = schedule,
                    pos = detailViewModel.tripScheduleList.indexOf(schedule)
                )
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}


@Composable
fun ScheduleItem(detailViewModel: DetailViewModel, schedule: TripScheduleModel, pos: Int) {

    // 일정 텍스트 컬러
    var textColor = wanderBlueColor

    // 일정 텍스트 폰트
    var textFont = CustomFont.customFontRegular

    // 일정 텍스트 컬러 조절
    textColor = if (!detailViewModel.menuStateMap[pos]!!) {
        Color.DarkGray
    } else {
        wanderBlueColor
    }

    // 일정 텍스트 폰트 조절
    textFont = if (!detailViewModel.menuStateMap[pos]!!) {
        CustomFont.customFontRegular
    } else {
        CustomFont.customFontBold
    }

    Column {
        // 일정 카드
        Card(
            modifier = Modifier
                .background(Color.Transparent) // 배경을 투명하게 설정
                .padding(vertical = 8.dp)
                .padding(start = 20.dp)
                .clickable {
                    detailViewModel.onClickIconScheduleCard(
                        pos,
                        schedule.scheduleDateList.size
                    )
                },
            shape = RoundedCornerShape(80),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${schedule.scheduleCity} 여행", color = textColor, fontFamily = textFont)
                val stDate = detailViewModel.convertToDate(schedule.scheduleStartDate)
                val edDate = detailViewModel.convertToMonthDayDate(schedule.scheduleEndDate)
                Row(
                    verticalAlignment = Alignment.CenterVertically // 세로 중앙 정렬
                ) {
                    Text("$stDate - $edDate", color = textColor, fontFamily = textFont)
                    Spacer(modifier = Modifier.width(10.dp))
                    if (detailViewModel.menuStateMap[pos]!!)
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_check_24px),
                            contentDescription = "Check Icon",
                            tint = wanderBlueColor // 아이콘 색상을 파란색으로 변경
                        )
                }
            }
        }
        // 일정 날짜별 카드
        if (detailViewModel.menuStateMap[pos]!!)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth() // Row가 화면을 가득 채우도록 설정
                    .padding(vertical = 8.dp) // 전체 Row에 여백 추가
            ) {
                // schedule.scheduleDateList.size 만큼 아이템 생성
                items(schedule.scheduleDateList) { date ->
                    if (!detailViewModel.scheduleDatePickerStateMap[schedule.scheduleDateList.indexOf(
                            date
                        )]!!
                    ) {
                        // 선택 안됐을 때 날자 카드
                        Card(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .clickable {
                                    detailViewModel.onClickScheduleDateCard(
                                        schedule.scheduleDateList.indexOf(
                                            date
                                        )
                                    )
                                }, // 카드 사이 여백
                            shape = RoundedCornerShape(80),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Day${schedule.scheduleDateList.indexOf(date) + 1}",
                                    fontFamily = CustomFont.customFontRegular
                                )
                                val dateMonthDay = detailViewModel.convertToMonthDayDate(date)
                                val day = detailViewModel.convertToDayOfWeekFromTimestamp(date)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "$dateMonthDay / ($day)",
                                        fontFamily = CustomFont.customFontRegular,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                            }
                        }
                    } else {
                        // 선택 됐을 때 날자 카드
                        Card(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .clickable {
                                    detailViewModel.onClickScheduleDateCard(
                                        schedule.scheduleDateList.indexOf(
                                            date
                                        )
                                    )
                                }, // 카드 사이 여백
                            shape = RoundedCornerShape(80),
                            colors = CardDefaults.cardColors(containerColor = Color.Blue)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Day${schedule.scheduleDateList.indexOf(date) + 1}",
                                    fontFamily = CustomFont.customFontBold, color = Color.White
                                )
                                val dateMonthDay = detailViewModel.convertToMonthDayDate(date)
                                val day = detailViewModel.convertToDayOfWeekFromTimestamp(date)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "$dateMonthDay / ($day)",
                                        fontFamily = CustomFont.customFontBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                            }
                        }
                    }
                }
            }
    }
}