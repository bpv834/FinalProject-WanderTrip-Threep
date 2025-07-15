package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.ScheduleRandomSelectItemViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound


@Composable
fun ScheduleRandomItemList(
    viewModel: ScheduleRandomSelectItemViewModel,
    onClickAddSchedule: (TripLocationBasedItem) -> Unit, // ✅ 클릭 이벤트 콜백 추가){}
    onClickShowItemDetail:(contentId:String)->Unit
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
                    .fillMaxWidth()
                    .height((sh / 18).dp)
                    .padding(start = 8.dp, end = 8.dp, top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 왼쪽 Column
                Column(
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxHeight()
                        .padding(end = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    ContentsInfo(
                        viewModel = viewModel,
                        tripLocationBasedItem = sortedTripItemList[index].publicData
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // ← 여기서 간격 조절
                    ) {
                        Text(
                            "상세보기",
                            fontSize = 13.sp,
                            fontFamily = NanumSquareRound,
                            modifier = Modifier.clickable {
                                onClickShowItemDetail(sortedTripItemList[index].publicData.contentId!!)
                            }
                        )

                        Text(
                            "추가하기",
                            fontSize = 13.sp,
                            fontFamily = NanumSquareRound,
                            modifier = Modifier.clickable {
                                onClickAddSchedule(sortedTripItemList[index].publicData)
                            }
                        )
                    }
                }

                // 오른쪽 이미지 + 하트 버튼
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        .padding(end = 8.dp)
                ) {
                    Image(
                        painter = if (sortedTripItemList[index].publicData.firstImage!!.isEmpty()) {
                            painterResource(id = R.drawable.ic_hide_image_144dp)
                        } else {
                            rememberAsyncImagePainter(model = sortedTripItemList[index].publicData.firstImage!!)
                        },
                        contentDescription = "Trip Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
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
                            .align(Alignment.BottomEnd)
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

            // 구분선
            CustomDividerComponent(10.dp)
        }
    }
}

