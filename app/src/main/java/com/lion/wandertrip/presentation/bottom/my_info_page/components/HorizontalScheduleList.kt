package com.lion.wandertrip.presentation.bottom.my_info_page.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.presentation.bottom.my_info_page.MyInfoViewModel
import com.lion.wandertrip.ui.theme.pastelColors
import com.lion.wandertrip.util.Tools
import kotlin.random.Random

@Composable
fun HorizontalScheduleList(viewModel : MyInfoViewModel) {
    val schedules by viewModel.userSchedules.collectAsState()
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp) // 아이템 간격
    ) {
        items(schedules) { schedule ->
            ScheduleItemView(viewModel,schedule)
        }
    }
}

@Composable
fun ScheduleItemView(viewModel :MyInfoViewModel, scheduleItem: TripScheduleModel) {
    Row(
        modifier = Modifier.clickable {
            viewModel.onClickScheduleItemGoScheduleDetail(scheduleItem.tripScheduleDocId,scheduleItem.scheduleCity)
        }
            .width(330.dp)
            .height(50.dp) // 아이템 높이 조정
            .background(pastelColors[Random.nextInt(pastelColors.size)], shape = RoundedCornerShape(10.dp)) // 배경 추가
            .padding(horizontal = 16.dp, vertical = 8.dp), // 내부 여백
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // 일정 이름과 남은 날짜를 좌우 정렬
    ) {
        // 일정 이름
        Text(text = "${scheduleItem.scheduleCity} 일정", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        // 남은 일정 (D-5 같은 형식)
        Text(text = "D-${Tools.getRemainingDays(scheduleItem.scheduleStartDate)}", fontSize = 14.sp, color = Color.Red)
    }
}
