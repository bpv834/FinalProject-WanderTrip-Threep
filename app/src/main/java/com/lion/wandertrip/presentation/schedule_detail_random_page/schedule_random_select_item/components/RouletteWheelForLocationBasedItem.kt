package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components


import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.ScheduleRandomSelectItemViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RouletteWheelForLocationBasedItem(
    rotationAngle: Float,
    viewModel : ScheduleRandomSelectItemViewModel
) {
    val items by viewModel.rouletteList.collectAsState()
    val itemCount = items.size
    val sliceAngle = if (itemCount > 0) 360f / itemCount else 360f
    val radius = with(LocalDensity.current) { 150.dp.toPx() }
    val colors = listOf(
        Color(0xFFFFC107), Color(0xFFFF5722), Color(0xFFE91E63), Color(0xFF3F51B5),
        Color(0xFF009688), Color(0xFF8BC34A), Color(0xFFFF9800), Color(0xFF673AB7)
    )
    Canvas(
        modifier = Modifier.size(320.dp)
    ) {
        if (items.isEmpty()) {
            // 아이템이 없으면 전체를 회색 원으로 그림

            // drawArc : Canvas에서 원호(Arc)를 그리는 함수예요
            drawArc(
                color = Color.Gray,         // 그릴 색상
                startAngle = 0f,           // 시작 각도 (0도는 오른쪽)
                sweepAngle = 360f,          // 몇 도까지 그릴 것인지 (90도면 1/4원)
                useCenter = true,          // true면 중심을 연결해 파이 모양 / false면 둥근 호만 그림
            )
        } else {
            // 아이템이 있을 때 각 아이템마다 룰렛 조각을 그림
            items.forEachIndexed { index, tripItem ->
                // 각 조각의 시작 각도를 계산 (현재 인덱스 * 조각 각도 + 전체 회전각)
                val startAngle = index * sliceAngle + rotationAngle

                // 텍스트를 배치하기 위한 중간 각도 계산
                val midAngle = startAngle + (sliceAngle / 2)

                // 회전된 상태에서 해당 조각을 그림
                rotate(startAngle) {
                    drawArc(
                        color = colors[index % colors.size], // 색상 리스트를 반복하여 적용
                        startAngle = 0f, // 회전된 좌표계 기준으로 시작
                        sweepAngle = sliceAngle, // 하나의 조각에 해당하는 각도
                        useCenter = true // 원 중심부터 조각을 채우는 형태로 그림 (파이 조각처럼)
                    )
                }

                // 텍스트를 배치할 반지름 위치 계산 (중심에서 약간 안쪽으로)
                val textRadius = radius * 0.7f

                // 중간 각도를 기준으로 텍스트 좌표 계산 (삼각함수로 위치 구함)
                val textX = center.x + textRadius * cos(Math.toRadians(midAngle.toDouble())).toFloat()
                val textY = center.y + textRadius * sin(Math.toRadians(midAngle.toDouble())).toFloat()

                // 텍스트를 해당 위치에 그림 (Android의 native Canvas 사용)
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        tripItem.title!!, // 아이템 제목
                        textX, // X좌표
                        textY, // Y좌표
                        Paint().apply {
                            color = android.graphics.Color.DKGRAY // 텍스트 색상
                            textSize = 45f // 텍스트 크기 (픽셀 단위)
                            textAlign = Paint.Align.CENTER // 텍스트 가운데 정렬
                            typeface = Typeface.DEFAULT_BOLD // 볼드체
                        }
                    )
                }
            }
        }
    }
}

