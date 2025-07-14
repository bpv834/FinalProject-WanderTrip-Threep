package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.ScheduleRandomSelectItemViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.util.AccommodationItemCat3.Companion.fromCodeAccommodationItemCat3
import com.lion.wandertrip.util.ContentTypeId
import com.lion.wandertrip.util.RestaurantItemCat3.Companion.fromCodeRestaurantItemCat3
import com.lion.wandertrip.util.TripItemCat2.Companion.fromCodeTripItemCat2


@Composable
fun ScheduleRandomItemList(
    viewModel: ScheduleRandomSelectItemViewModel,
    onItemClick: (TripLocationBasedItem) -> Unit // ✅ 클릭 이벤트 콜백 추가){}
) {
    val contentsMap by viewModel.allContentsMapFlow.collectAsState()
    val sortedTripItemList by viewModel.itemList.collectAsState()
    val userLikeList by viewModel.userLikeList.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(sortedTripItemList.size) { index ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(sortedTripItemList[index].publicData) } // ✅ 클릭 이벤트 추가
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 8.dp,
                            end = 8.dp,
                            top = 20.dp,
                            bottom = 8.dp
                        ), // ✅ 클릭 이벤트 추가,
                    verticalAlignment = Alignment.Top
                ) {

                    Column(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .weight(1f) // ✅ 너비 조정
                    ) {
                        // ✅ 제목 텍스트
                        Text(
                            text = sortedTripItemList[index].publicData.title!!,
                            fontSize = 20.sp,
                            fontFamily = NanumSquareRound,
                            maxLines = 1, // ✅ 최대 1줄까지 표시
                            overflow = TextOverflow.Ellipsis, // ✅ 너무 길면 "..." 표시
                            modifier = Modifier.padding(bottom = 5.dp)
                        )

                        // ✅ 주소 텍스트
                        Text(
                            text = sortedTripItemList[index].publicData.addr1 + sortedTripItemList[index].publicData.addr2,
                            fontSize = 12.sp,
                            fontFamily = NanumSquareRoundRegular,
                            maxLines = 3, // ✅ 최대 2줄까지 표시
                            overflow = TextOverflow.Ellipsis, // ✅ 너무 길면 "..." 표시
                            lineHeight = 14.sp // ✅ 줄 간격을 조정 (기본값보다 약간 좁게)
                        )
                        val contentModel =
                            contentsMap[sortedTripItemList[index].publicData.contentId]
                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )
                        Row {
                            Icon(
                                imageVector = Icons.Default.Favorite, // 또는 다른 하트 아이콘
                                contentDescription = "관심 지역",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(3.dp)
                            )

                            Text(
                                text = "${contentModel?.interestingCount?:0}",
                                fontSize = 15.sp,
                                fontFamily = NanumSquareRoundRegular,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Icon(
                                imageVector = Icons.Default.Star, // 또는 다른 하트 아이콘
                                contentDescription = "평점",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(3.dp)
                            )

                            Text(
                                text = "${contentModel?.ratingScore?:0.0}",
                                fontSize = 15.sp,
                                fontFamily = NanumSquareRoundRegular,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )
                        }
                    }
                    // 이미지 영역: Box를 사용하여 하트 아이콘 오버레이
                    Box(
                        modifier = Modifier
                            .size(width = 140.dp, height = 120.dp)
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

                        // 하트 아이콘 버튼
                        if (userLikeList.contains(sortedTripItemList[index].publicData.contentId)) {
                            IconButton(
                                onClick = {
                                    viewModel.removeLikeItem(sortedTripItemList[index].publicData.contentId!!)
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite, // 또는 다른 하트 아이콘
                                    contentDescription = "관심 지역",
                                    tint = Color.Red
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    viewModel.addLikeItem(sortedTripItemList[index].publicData.contentId!!)
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder, // 또는 다른 하트 아이콘
                                    contentDescription = "관심 지역 X",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }


            }

            CustomDividerComponent(10.dp)
        }

    }
}

