package com.lion.wandertrip.presentation.schedule_select_item.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import com.lion.wandertrip.model.TripItemModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RouletteWheelForTripItems(
    items: List<TripItemModel>,
    rotationAngle: Float
) {
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
            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true
            )
        } else {
            items.forEachIndexed { index, tripItem ->
                val startAngle = index * sliceAngle + rotationAngle
                val midAngle = startAngle + (sliceAngle / 2)

                rotate(startAngle) {
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = 0f,
                        sweepAngle = sliceAngle,
                        useCenter = true
                    )
                }

                val textRadius = radius * 0.7f
                val textX = center.x + textRadius * cos(Math.toRadians(midAngle.toDouble())).toFloat()
                val textY = center.y + textRadius * sin(Math.toRadians(midAngle.toDouble())).toFloat()

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        tripItem.title,
                        textX,
                        textY,
                        Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 45f
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoulettePointerForTripItems() {
    Canvas(
        modifier = Modifier.size(320.dp)
    ) {
        val pointerWidth = 80f    // 화살표 너비
        val pointerHeight = 120f   // 화살표 높이 (룰렛을 살짝 넘어가도록)
        val cornerRadius = 15f     // 모서리 둥글게

        val centerX = size.width / 2
        val centerY = 0f           // 상단 (12시 방향)
        // 화살표 좌표 계산
        val pointerTop = Offset(centerX, centerY + pointerHeight * 0.6f)
        val pointerLeft = Offset(centerX - pointerWidth / 2, centerY - pointerHeight * 0.4f)
        val pointerRight = Offset(centerX + pointerWidth / 2, centerY - pointerHeight * 0.4f)

        val pointerPath = Path().apply {
            moveTo(pointerTop.x, pointerTop.y)
            lineTo(pointerRight.x, pointerRight.y)
            arcTo(
                rect = Rect(
                    topLeft = Offset(pointerRight.x - cornerRadius, pointerRight.y - cornerRadius),
                    bottomRight = Offset(pointerLeft.x + cornerRadius, pointerLeft.y + cornerRadius)
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            lineTo(pointerLeft.x, pointerLeft.y)
            close()
        }

        // 포인터 채우기
        drawPath(
            path = pointerPath,
            color = Color.Red,
            style = Fill
        )
        // 테두리 효과
        drawPath(
            path = pointerPath,
            color = Color.Black,
            style = Stroke(width = 5f)
        )
    }
}