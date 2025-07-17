package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components

import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
    onConfirm: (item:TripLocationBasedItem) -> Unit,
    onAddPlaceClick: () -> Unit // 여행지 추가 다이얼로그 띄우기용 콜백 추가
) {
    val coroutineScope = rememberCoroutineScope()
    val animatedRotation = remember { Animatable(0f) } // Animatable.animateTo()를 통해 부드러운 회전 애니메이션을 할 수 있다
    var selectedItem by remember { mutableStateOf<TripLocationBasedItem?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    val rouletteList by viewModel.rouletteList.collectAsState()



    val sh = viewModel.application.screenHeight

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = (sh / 6).dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp)) // 배경에 둥근 사각형 적용
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.matchParentSize(), // Box 높이만큼 Column이 확장됨
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
                Spacer(modifier = Modifier.weight(1f)) // 여기에 내용 들어올 수 있음

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
                            // 코루틴을 실행해서 비동기로 애니메이션 처리
                            coroutineScope.launch {
                                // 720도에서 1440도 사이의 랜덤 회전각 생성 (최소 2바퀴 ~ 최대 4바퀴 회전)
                                val randomRotation = Random.nextInt(720, 1440).toFloat()

                                // 현재 회전값에서 랜덤값만큼 더한 값으로 애니메이션 실행 (부드럽게 회전)

                                // 현재 값에서 목표값까지 부드럽게, 애니메이션으로 변경하겠다”
                                //라는 명령이고,
                                //실제 값(animatedRotation.value)은 애니메이션 진행에 따라 서서히 바뀌는 것
                                // 결국 animatedRotation 값은 타겟밸류로 서서히 변한다.
                                animatedRotation.animateTo(
                                    targetValue = animatedRotation.value + randomRotation,
                                    animationSpec = tween(
                                        durationMillis = 2500,                 // 애니메이션이 2.5초 동안 실행됨
                                        easing = FastOutSlowInEasing           // 속도 변화 처음엔 빠르게, 끝엔 느리게
                                    )
                                )

                                // 최종 회전 각도를 0~360 사이의 값으로 정규화 (한 바퀴 내 위치 계산)
                                val finalRotation = animatedRotation.value % 360

                                // 룰렛 항목 개수
                                val itemCount = rouletteList.size

                                // 각 항목이 차지하는 각도 계산 (예: 5개면 360/5 = 72도)
                                val sliceAngle = if (itemCount > 0) 360f / itemCount else 360f

                                // 실제 선택된 인덱스를 계산
                                val selectedIndex = if (itemCount > 0)
                                // 룰렛의 기준점이 위쪽(270도)이므로, 해당 위치에 있는 항목 인덱스를 계산
                                    (((270f - finalRotation + 360) % 360) / sliceAngle).toInt() % itemCount
                                else -1 // 항목이 없을 경우 -1 반환

                                // 유효한 인덱스일 경우 해당 항목을 선택된 항목으로 설정
                                if (selectedIndex >= 0) {
                                    selectedItem = rouletteList[selectedIndex]
                                }

                                // 결과 다이얼로그를 보여주기 위한 플래그 설정
                                showResultDialog = true

                                // 디버깅 로그 출력
                                Log.d("selectedIndex","selectedIndex:$selectedIndex")
                                Log.d("selectedItem","selectedItem:$selectedItem")
                            }
                        },
                        enabled = rouletteList.isNotEmpty(),
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
