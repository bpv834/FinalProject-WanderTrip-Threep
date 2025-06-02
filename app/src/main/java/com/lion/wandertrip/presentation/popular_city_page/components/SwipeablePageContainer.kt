package com.lion.wandertrip.presentation.popular_city_page.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SwipeablePageContainer(
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    content: @Composable () -> Unit
) {
    val swipeState = rememberSwipeableState(initialValue = 0)

    val anchors = mapOf(
        0f to 0,        // 기본 위치
        -300f to -1,    // 왼쪽으로 스와이프
        300f to 1       // 오른쪽으로 스와이프
    )

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .swipeable(
                state = swipeState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
    ) {
        content() // 👉 이 부분 중요: 매개변수로 받은 content를 박스 안에 배치

        LaunchedEffect(swipeState.currentValue) {
            when (swipeState.currentValue) {
                -1 -> {
                    onSwipeLeft()
                    scope.launch { swipeState.snapTo(0) }
                }

                1 -> {
                    onSwipeRight()
                    scope.launch { swipeState.snapTo(0) }
                }
            }
        }
    }
}