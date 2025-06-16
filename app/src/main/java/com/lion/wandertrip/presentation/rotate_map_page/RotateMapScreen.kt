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
import com.lion.wandertrip.presentation.rotate_map_page.components.NoAttractionDialog
import com.lion.wandertrip.presentation.rotate_map_page.components.TravelConfirmDialog
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

    // ViewModel 상태
    val isSpinning by viewModel.isSpinning.collectAsState()
    val targetRotation by viewModel.targetRotation.collectAsState()

    //  애니메이션 상태
    val animRotation = remember { Animatable(0f) }

    // 지역 상태
    val scope = rememberCoroutineScope()
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    var relativeClick by remember { mutableStateOf<Offset?>(null) }
    var wanderTripCount by remember { mutableStateOf(1) }
    var retryCount by remember { mutableStateOf(3) }

    val showAttractionDialog by viewModel.showAttractionDialog.collectAsState()
    val showNoAttractionDialog by viewModel.showNoAttractionDialog.collectAsState()

    // 애니메이션 트리거
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
                    .onSizeChanged { mapSize = it }
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            if (mapSize.width > 0 && mapSize.height > 0 && isSpinning) {
                                val correctedOffset = viewModel.rotatePointBack(
                                    tap,
                                    mapSize,
                                    animRotation.value
                                )
                                relativeClick = correctedOffset

                                val (lat, lon) = viewModel.toLatLng(correctedOffset)
                                viewModel.stopSpinning()

                                val current = animRotation.value % 360f
                                val target = animRotation.value + (360f - current) + 360f
                                viewModel.setTargetRotation(target)
                                viewModel.onRotationFinished(lat.toString(), lon.toString())
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = animRotation.value
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

                    if (showAttractionDialog) {
                        TravelConfirmDialog(
                            onYesClick = { viewModel.hideAttractionDialog() },
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
                                    animRotation.snapTo(current + 10f)
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