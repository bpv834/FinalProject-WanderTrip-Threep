package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.ScheduleRandomSelectItemViewModel
import com.lion.wandertrip.presentation.schedule_select_item.roulette_item.component.RoulettePointerForTripItems
import com.lion.wandertrip.presentation.schedule_select_item.roulette_item.component.RouletteWheelForTripItems
import com.lion.wandertrip.util.SharedTripItemList
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun RouletteDialog(
    viewModel : ScheduleRandomSelectItemViewModel,
    onDismiss: () -> Unit,
    onConfirm: (TripLocationBasedItem) -> Unit,
    onAddPlaceClick: () -> Unit // 여행지 추가 다이얼로그 띄우기용 콜백 추가
) {
    val coroutineScope = rememberCoroutineScope()
    val animatedRotation = remember { Animatable(0f) }
    var selectedItem by remember { mutableStateOf<TripLocationBasedItem?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    val sh = viewModel.application.screenHeight

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxWidth()
                .defaultMinSize(minHeight = (sh / 7).dp)
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f), // 정사각형으로 유지 (룰렛용)
                    contentAlignment = Alignment.Center
                ) {
                    // 회전하는 룰렛 휠
                    RouletteWheelForLocationBasedItem(
                        viewModel = viewModel,
                        rotationAngle = animatedRotation.value
                    )

                    // 고정된 포인터 (12시 방향)
                    RoulettePointerForTripItems()
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 여행지 추가하기 버튼
                    Button(
                        onClick = onAddPlaceClick,
                        shape = CircleShape
                    ) {
                        Text("여행지 추가하기")
                    }

                    // 룰렛 돌리기 버튼
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val randomRotation = Random.nextInt(720, 1440).toFloat()
                                animatedRotation.animateTo(
                                    targetValue = animatedRotation.value + randomRotation,
                                    animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
                                )

                                val finalRotation = animatedRotation.value % 360
                                val itemCount = SharedTripItemList.rouletteItemList.size
                                val sliceAngle = if (itemCount > 0) 360f / itemCount else 360f
                                val selectedIndex = if (itemCount > 0)
                                    (((270f - finalRotation + 360) % 360) / sliceAngle).toInt() % itemCount
                                else -1

                                if (selectedIndex >= 0) {
                                   // selectedItem = SharedTripItemList.rouletteItemList[selectedIndex]
                                }
                                showResultDialog = true
                            }
                        },
                        enabled = SharedTripItemList.rouletteItemList.isNotEmpty(),
                        shape = CircleShape
                    ) {
                        Text("룰렛 돌리기")
                    }
                }
            }
        }
    }
    if (showResultDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("🎉 선택된 여행지") },
            text = { Text("당신의 여행지는 \"${selectedItem?.title}\" 입니다!") },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        onConfirm(selectedItem!!)
                    }
                ) {
                    Text("결정하기")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showResultDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF435C8F)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF435C8F))
                ) {
                    Text("다시하기")
                }
            }
        )
    }
}
