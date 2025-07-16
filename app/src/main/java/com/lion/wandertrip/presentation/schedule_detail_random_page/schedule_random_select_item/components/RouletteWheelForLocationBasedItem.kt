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
                        tripItem.title!!,
                        textX,
                        textY,
                        Paint().apply {
                            color = android.graphics.Color.DKGRAY
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

