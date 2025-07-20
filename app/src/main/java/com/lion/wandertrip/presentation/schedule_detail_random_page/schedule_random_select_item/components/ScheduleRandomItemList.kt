package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.ScheduleRandomSelectItemViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage


@Composable
fun ScheduleRandomItemList(
    viewModel: ScheduleRandomSelectItemViewModel,
    onClickAddSchedule: (TripLocationBasedItem) -> Unit, // ✅ 클릭 이벤트 콜백 추가){}
    onClickShowItemDetail: (contentId: String) -> Unit
) {
    val sortedTripItemList by viewModel.itemList.collectAsState()
    val userLikeList by viewModel.userLikeList.collectAsState()
    val sh = viewModel.application.screenHeight

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(sortedTripItemList.size) { index ->
            Row(
                modifier = Modifier
                    .height((sh / 16).dp)
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = Color.LightGray,
                            start = androidx.compose.ui.geometry.Offset(
                                0f,
                                size.height - strokeWidth / 2
                            ),
                            end = androidx.compose.ui.geometry.Offset(
                                size.width,
                                size.height - strokeWidth / 2
                            ),
                            strokeWidth = strokeWidth
                        )
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxHeight()
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    // 버튼 Row
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "상세보기",
                            fontSize = 16.sp,
                            fontFamily = NanumSquareRound,
                            modifier = Modifier.clickable {
                                onClickShowItemDetail(sortedTripItemList[index].publicData.contentId!!)
                            }
                        )
                        Text(
                            "추가하기",
                            fontSize = 16.sp,
                            fontFamily = NanumSquareRound,
                            modifier = Modifier.clickable {
                                onClickAddSchedule(sortedTripItemList[index].publicData)
                            }
                        )
                    }

                    // ContentsInfo
                    ContentsInfo(
                        viewModel = viewModel,
                        tripLocationBasedItem = sortedTripItemList[index].publicData,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                    )
                }
                // 오른쪽 이미지 + 하트 버튼
                Box(
                    modifier = Modifier
                        .fillMaxHeight() // Row의 높이에 맞춰줌
                        .weight(2f)
                ) {
                    GlideImage(
                        imageModel = if (sortedTripItemList[index].publicData.firstImage!!.isEmpty()) R.drawable.img_image_holder
                        else sortedTripItemList[index].publicData.firstImage!!,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp)),  // 이미지 둥글게 만들기
                        circularReveal = CircularReveal(duration = 250),
                        placeHolder = ImageBitmap.imageResource(R.drawable.img_image_holder),
                    )

                    // 하트 버튼
                    val contentId = sortedTripItemList[index].publicData.contentId!!
                    IconButton(
                        onClick = {
                            if (userLikeList.contains(contentId)) {
                                viewModel.removeLikeItem(contentId)
                            } else {
                                viewModel.addLikeItem(contentId)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (userLikeList.contains(contentId))
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            contentDescription = "관심 지역",
                            tint = if (userLikeList.contains(contentId)) Color.Red else Color.Red
                        )
                    }
                }
            }
        }
    }
}

