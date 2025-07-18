package com.lion.wandertrip.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun ScheduleDetailCustomMarkerIcon(context: Context, index: Int): BitmapDescriptor {
    // 아이콘 크기 (dp 단위)
    val sizeDp = 30
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 원형 배경 그리기
    val paintCircle = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }
    canvas.drawCircle(
        (sizePx / 2).toFloat(),
        (sizePx / 2).toFloat(),
        (sizePx / 2).toFloat(),
        paintCircle
    )

    // 가운데 itemIndex 텍스트 그리기
    val paintText = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = sizePx / 2f
        isAntiAlias = true
    }
    // 텍스트의 세로 중앙 정렬을 위한 계산
    val xPos = sizePx / 2f
    val yPos = (sizePx / 2f) - ((paintText.descent() + paintText.ascent()) / 2)
    canvas.drawText(index.toString(), xPos, yPos, paintText)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}