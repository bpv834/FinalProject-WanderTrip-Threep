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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.lion.wandertrip.presentation.schedule_select_item.roulette_item.component.RoulettePointerForTripItems
import com.lion.wandertrip.presentation.schedule_select_item.roulette_item.component.RouletteWheelForTripItems
import com.lion.wandertrip.util.SharedTripItemList
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun RouletteDialog(
    onDismiss: () -> Unit,
    onConfirm: (TripItemModel) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val animatedRotation = remember { Animatable(0f) }
    var selectedItem by remember { mutableStateOf<TripItemModel?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = CircleShape)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("룰렛 돌리기", fontSize = 20.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Box(contentAlignment = Alignment.Center) {
                    RouletteWheelForTripItems(
                        items = SharedTripItemList.rouletteItemList,
                        rotationAngle = animatedRotation.value
                    )
                    RoulettePointerForTripItems()
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF435C8F)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF435C8F))
                    ) {
                        Text("닫기")
                    }

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
                                    selectedItem = SharedTripItemList.rouletteItemList[selectedIndex]
                                }
                                showResultDialog = true
                            }
                        },
                        enabled = SharedTripItemList.rouletteItemList.isNotEmpty(),
                        modifier = Modifier.padding(8.dp),
                        shape = CircleShape
                    ) {
                        Text("룰렛 돌리기")
                    }
                }
            }
        }
    }

    // 결과 다이얼로그
    if (showResultDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("🎉 선택된 도시") },
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
