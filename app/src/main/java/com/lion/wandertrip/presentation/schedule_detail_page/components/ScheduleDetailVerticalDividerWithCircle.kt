package com.lion.wandertrip.presentation.schedule_detail_page.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape

@Composable
fun ScheduleDetailVerticalDividerWithCircle(
    modifier: Modifier = Modifier,
    dividerThickness: Dp = 3.dp,
    dividerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    circleSize: Dp = 15.dp,
    circleColor: Color = dividerColor
) {
    Box(modifier = modifier) {
        // 수직선: Box의 중앙에 정렬 (수평 중앙)
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(dividerThickness)
                .background(color = dividerColor)
                .align(Alignment.Center)
        )
        // 원: 상단 중앙에 배치하고 탑 패딩 10.dp 적용
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(circleSize)
                .clip(CircleShape)
                .background(color = Color(0xffd0e4fe))
                .align(Alignment.TopCenter)
        )
    }
}