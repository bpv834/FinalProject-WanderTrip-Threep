package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.ScheduleRandomSelectItemViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound

@Composable
fun AddItemDialog(
    viewModel: ScheduleRandomSelectItemViewModel,
    onAdd: (List<TripLocationBasedItem>) -> Unit, // ✅ 여러 개 추가
    onDismiss: () -> Unit
) {
    val itemList by viewModel.itemList.collectAsState()
    val sh = viewModel.application.screenHeight

    // ✅ 선택 상태 저장용 StateList
    val isChecked = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(itemList.size) { add(false) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height((sh / 2).dp) // 높이 조금 넉넉하게
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(itemList.size) { idx ->
                        val item = itemList[idx].publicData
                        val selected = isChecked[idx]

                        // ✅ 클릭 시 선택 상태 토글
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .height(48.dp)
                                .then(
                                    if (selected) Modifier.padding(4.dp).background(Color(0xFFE0F7FA)) // 선택된 배경
                                    else Modifier
                                )
                                .padding(horizontal = 8.dp)
                                .clickable {
                                    isChecked[idx] = !isChecked[idx]
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.title ?: "제목 없음",
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )

                            if (selected) {
                                Text("✔", color = Color(0xFF00796B), fontSize = 14.sp)
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onDismiss) {
                        Text("닫기")
                    }

                    Button(
                        onClick = {
                            // ✅ 선택된 아이템만 필터링해서 onAdd 전달
                            val selectedItems = itemList
                                .filterIndexed { index, _ -> isChecked.getOrNull(index) == true }
                                .map { it.publicData }
                            onAdd(selectedItems)
                            onDismiss()
                        }
                    ) {
                        Text("추가하기")
                    }
                }
            }
        }
    }
}