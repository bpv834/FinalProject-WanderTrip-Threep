package com.lion.wandertrip.presentation.rotate_map_page

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotateMapScreen(
    scheduleTitle: String,
    scheduleStartDate: Timestamp,
    scheduleEndDate: Timestamp,
    viewModel: RotateMapViewModel = hiltViewModel(),
) {
    val context = viewModel.tripApplication
    val image = painterResource(id = R.drawable.img_south_korea_map)
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var wanderTripCount by remember { mutableStateOf(1) }
    var retryCount by remember { mutableStateOf(3) }

    val scope = rememberCoroutineScope()
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    var relativeClick by remember { mutableStateOf<Offset?>(null) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CustomTopAppBar(
                title = "룰렛 돌리기",
                navigationIconImage = Icons.Filled.ArrowBack,
                navigationIconOnClick = {
                    context.navHostController.popBackStack()
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onSizeChanged { mapSize = it }
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            if (mapSize.width > 0 && mapSize.height > 0) {
                                // 클릭 위치 회전 보정
                                val correctedOffset = rotatePointBack(tap, mapSize, rotation.value)
                                relativeClick = correctedOffset

                                // 회전 후 위도/경도 계산
                                val (lat, lon) = toLatLng(correctedOffset)
                                println("🎯 위도: $lat, 경도: $lon")

                                if (isSpinning) {
                                    isSpinning = false
                                    scope.launch {
                                        val current = rotation.value % 360f
                                        val target = rotation.value + (360f - current) + 360f
                                        rotation.animateTo(
                                            targetValue = target,
                                            animationSpec = tween(2000, easing = LinearOutSlowInEasing)
                                        )

                                        // 회전 후 위도/경도 계산
                                        val (lat, lon) = toLatLng(correctedOffset)
                                        println("🎯 위도: $lat, 경도: $lon")
                                    }
                                }
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = rotation.value
                            transformOrigin = TransformOrigin.Center
                        }
                ) {
                    Image(
                        painter = image,
                        contentDescription = "지도",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    relativeClick?.let { offset ->
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.Red,
                                radius = 12f,
                                center = Offset(
                                    x = offset.x * size.width,
                                    y = offset.y * size.height
                                )
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (!isSpinning) {
                            isSpinning = true
                            relativeClick = null
                            wanderTripCount += 1
                            scope.launch {
                                while (isSpinning) {
                                    rotation.snapTo(rotation.value + 10f)
                                    delay(16L)
                                }
                            }
                        }
                    }
                ) {
                    Text("돌리기")
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("wanderTrip 횟수: $wanderTripCount", fontSize = 18.sp)
                Text("retry: $retryCount", fontSize = 18.sp)
            }
        }
    }
}

// 회전 보정 함수
fun rotatePointBack(tap: Offset, size: IntSize, rotationDegrees: Float): Offset {
    val center = Offset(size.width / 2f, size.height / 2f)

    val dx = tap.x - center.x
    val dy = tap.y - center.y

    val radians = Math.toRadians(-rotationDegrees.toDouble())

    val rotatedX = (dx * cos(radians) - dy * sin(radians)).toFloat()
    val rotatedY = (dx * sin(radians) + dy * cos(radians)).toFloat()

    val corrected = Offset(rotatedX + center.x, rotatedY + center.y)

    return Offset(
        corrected.x / size.width,
        corrected.y / size.height
    )
}

// 위도/경도 변환 함수
fun toLatLng(relativeOffset: Offset): Pair<Double, Double> {
    val latTop = 38.64874
    val latBottom = 33.19874
    val lonLeft = 125.99130
    val lonRight = 129.59130

    val latitude = latTop + (latBottom - latTop) * relativeOffset.y
    val longitude = lonLeft + (lonRight - lonLeft) * relativeOffset.x

    return latitude to longitude
}
