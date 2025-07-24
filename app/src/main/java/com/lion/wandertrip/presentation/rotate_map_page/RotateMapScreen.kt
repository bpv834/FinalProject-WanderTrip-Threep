package com.lion.wandertrip.presentation.rotate_map_page

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.lion.wandertrip.presentation.rotate_map_page.components.NoAttractionDialog
import com.lion.wandertrip.presentation.rotate_map_page.components.TravelConfirmDialog
import com.lion.wandertrip.util.Tools
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun RotateMapScreen(
    scheduleTitle: String,
    scheduleStartDate: Timestamp,
    scheduleEndDate: Timestamp,
    viewModel: RotateMapViewModel = hiltViewModel(),
) {
    val context = viewModel.tripApplication
    val image = painterResource(id = R.drawable.img_south_korea_map)

    val animRotation = remember { Animatable(0f) } // 현재 회전 상태를 기억하고 애니메이션으로 변경할 수 있도록 하는 객체

    val isSpinning by viewModel.isSpinning.collectAsState()
    val targetRotation by viewModel.targetRotation.collectAsState() // 목표 회전각.

    val scope = rememberCoroutineScope()
    var mapSize by remember { mutableStateOf(IntSize.Zero) } // 박스 사이즈 저장변수
    var relativeClick by remember { mutableStateOf<Offset?>(null) }
    var wanderTripCount by remember { mutableStateOf(1) }
    var retryCount by remember { mutableStateOf(3) }

    val showAttractionDialog by viewModel.showAttractionDialog.collectAsState()
    val showNoAttractionDialog by viewModel.showNoAttractionDialog.collectAsState()

    // targetRotation 값이 변경될때마다 각도를 변환시킨다.
    LaunchedEffect(targetRotation) {
        animRotation.animateTo(
            targetValue = targetRotation,
            animationSpec = tween(2000, easing = LinearOutSlowInEasing)
        )
    }
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
                    .graphicsLayer {
                        rotationZ = animRotation.value // 2D 평면에서 시계 방향 회전 각도
                        transformOrigin = TransformOrigin.Center //회전할 때 중심을 기준으로 돌겠다
                    }
                    .onSizeChanged { mapSize = it }
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            if (mapSize.width > 0 && mapSize.height > 0 && isSpinning) {
                                // 역회전 보정
                                // 돌린만큼 거꾸로 돌리기 그래야 정위치 포인터값을 알기때문
                                val correctedOffset = viewModel.rotatePointBack(
                                    tap,
                                    mapSize,
                                    animRotation.value // 전체 회전각도 몇바퀴 얼마나 돌았는지
                                )
                                relativeClick = correctedOffset

                                val (lat, lon) = viewModel.toLatLng(correctedOffset)
                                viewModel.setLatLng(lat.toString(), lon.toString())
                                viewModel.stopSpinning()

                                val current = animRotation.value % 360f //  (예: 725도 → 5도).
                                val target = animRotation.value + (360f - current) + 360f // (예: 5도면 → 355도) 나머지 회전후 정위치에서 멈추는 각도
                                viewModel.setTargetRotation(target)

                                viewModel.onRotationFinished(lat.toString(), lon.toString())
                            }
                        }
                    }
                    .border(
                        width = 2.dp,
                        brush = SolidColor(Color.Red),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
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
                            viewModel.startSpinning()
                            relativeClick = null
                            wanderTripCount += 1
                            scope.launch {
                                while (viewModel.isSpinning.value) {
                                    val current = animRotation.value
                                    animRotation.snapTo(current + 10f) // 현재각에서 10도씩 회전
                                    delay(16L) // 60fps 프레임 16/1000ms
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
    // 다이얼로그는 스케폴드 바깥에 배치
    if (showAttractionDialog) {
        val regionName = Tools.getSimpleRegionName(
            viewModel.tripApplication,
            viewModel.initLat.toDouble(),
            viewModel.initLng.toDouble()
        )
        TravelConfirmDialog(
            onYesClick = {
                viewModel.addTripSchedule(
                    scheduleTitle,
                    scheduleStartDate,
                    scheduleEndDate,
                    regionName ?: "region",
                    viewModel.initLat.toDouble(),
                    viewModel.initLng.toDouble()
                )
            },
            onRetryClick = { viewModel.hideAttractionDialog() },
            onDismiss = { viewModel.hideAttractionDialog() }
        )
    }

    if (showNoAttractionDialog) {
        NoAttractionDialog(
            onYesClick = { viewModel.hideNoAttractionDialog() },
            onNoClick = { viewModel.hideNoAttractionDialog() },
            onDismiss = { viewModel.hideNoAttractionDialog() }
        )
    }

}