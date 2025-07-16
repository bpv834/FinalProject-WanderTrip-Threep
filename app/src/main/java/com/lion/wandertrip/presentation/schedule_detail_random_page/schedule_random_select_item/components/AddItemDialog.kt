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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.ScheduleRandomSelectItemViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun AddItemDialog(
    viewModel: ScheduleRandomSelectItemViewModel,
    onDismiss: () -> Unit,
    onClickAddAll : ()->Unit,
    onClickResetToSelectedItems : ()->Unit,
) {
    val itemList by viewModel.itemList.collectAsState()
    val contentsMap by viewModel.allContentsMapFlow.collectAsState()
    val sh = viewModel.application.screenHeight

    val selectedMap by viewModel.selectedMap.collectAsState()


    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn((sh / 12).dp) // 높이 조금 넉넉하게
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .weight(1f)
                ) {
                    items(itemList.size) { idx ->
                        val item = itemList[idx].publicData
                        val isSelected = selectedMap.containsKey(item.contentId)
                        Row(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) Color(0xFFE0F7FA) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp) // 선택적으로 모서리 둥글게
                                )
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { viewModel.toggleItem(item) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ✅ 왼쪽 텍스트 영역
                            Column(
                                modifier = Modifier
                                    .weight(3f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = item.title ?: "제목 없음",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, // ✅ 이걸 추가해야 말줄임표 처리됨
                                    fontSize = 15.sp,
                                    fontFamily = NanumSquareRound,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "❤️ ${contentsMap[item.contentId]?.interestingCount ?: 0}",
                                        fontSize = 13.sp,
                                        fontFamily = NanumSquareRound
                                    )
                                    Text(
                                        text = "⭐ ${contentsMap[item.contentId]?.ratingScore ?: 0}",
                                        fontSize = 13.sp,
                                        fontFamily = NanumSquareRound
                                    )
                                }
                            }

                            // ✅ 오른쪽 이미지 영역
                            if (item.firstImage != "")
                                GlideImage(
                                    imageModel = item.firstImage,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .height((sh / 24).dp)
                                        .weight(2f)
                                        .clip(RoundedCornerShape(8.dp)),  // 이미지 둥글게 만들기
                                    circularReveal = CircularReveal(duration = 250),
                                    placeHolder = ImageBitmap.imageResource(R.drawable.img_image_holder),
                                )
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onClickResetToSelectedItems() }, // 전체 제거
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("clear")
                    }

                    Button(
                        onClick = { onClickAddAll() }, // 전체 추가
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("addAll")
                    }

                    Button(onClick = onDismiss) { // 닫기
                        Text("X")
                    }
                }

            }
        }
    }
}