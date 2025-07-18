package com.lion.wandertrip.presentation.my_review_page.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.a02_boardcloneproject.component.CustomIconButton
import com.lion.wandertrip.R
import com.lion.wandertrip.component.CustomRatingBar
import com.lion.wandertrip.model.ReviewModel
import com.lion.wandertrip.presentation.my_review_page.MyReviewViewModel
import com.lion.wandertrip.ui.theme.LightGray3
import com.lion.wandertrip.util.CustomFont
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage


@Composable
fun VerticalMyReviewList(myReviewViewModel: MyReviewViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(myReviewViewModel.reviewList) { review ->
            val index = myReviewViewModel.reviewList.indexOf(review)
            ReviewItem(review = review, pos = index, myReviewViewModel)
        }
    }
}


@Composable
fun ReviewItem(review: ReviewModel, pos: Int, myReviewViewModel: MyReviewViewModel) {
    val sh = myReviewViewModel.tripApplication.screenHeight
    // 글자 줄이기
    val shortedContent = if (review.reviewContent.length > 20) {
        review.reviewContent.take(20) + "..."
    } else {
        review.reviewContent
    }
    val sH = myReviewViewModel.tripApplication.screenHeight
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray3)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // 리뷰 제목
            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp, top = 15.dp)
                    .padding(horizontal = 10.dp),
                text = review.reviewTitle,
                fontFamily = CustomFont.customFontBold,
                fontSize = 24.sp // 글씨 크기 설정
            )
            Row(
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {

                //별점 + 22년 2월 여행
                CustomRatingBar(review.reviewRatingScore)
                // 날짜
                Text(
                    "${myReviewViewModel.convertToDateMonth(review.reviewTimeStamp)} 여행",
                    fontFamily = CustomFont.customFontRegular,
                    modifier = Modifier.padding(start = 20.dp)
                )
            }


            // 리뷰 내용
            Text(
                modifier = Modifier
                    .padding(bottom = 30.dp, top = 15.dp)
                    .padding(horizontal = 10.dp),
                text = "내용 : ${shortedContent}",
                fontFamily = CustomFont.customFontRegular,
            )

            Column(
                modifier = Modifier.padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                // 이미지
                // 이미지 가로 스크롤
                val imgList = myReviewViewModel.reviewList[pos].reviewImageList

                if (imgList.isNotEmpty()) {  // 리스트가 비어 있지 않으면
                    LazyRow {
                        items(imgList.size) { index ->
                            GlideImage(
                                imageModel = imgList[index],
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((sh / 10).dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                circularReveal = CircularReveal(duration = 250),
                                placeHolder = ImageBitmap.imageResource(R.drawable.img_image_holder),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                } else {
                    // 리스트가 비었을 때 기본적인 메시지나 이미지를 표시하거나 아무것도 표시하지 않음
                    Text(text = "이미지가 없습니다.") // 예시
                }
            }

            // 우측 날짜와 팝업 메뉴
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(15.dp).fillMaxWidth()
            ) {
                when (myReviewViewModel.calculationDate(review.reviewTimeStamp)) {
                    0 -> {
                        Text(text = "오늘", fontFamily = CustomFont.customFontRegular)
                    }

                    1 -> {
                        Text(text = "하루 전", fontFamily = CustomFont.customFontRegular)
                    }

                    2 -> {
                        Text(text = "이틀 전", fontFamily = CustomFont.customFontRegular)
                    }

                    3 -> {
                        Text(text = "사흘 전", fontFamily = CustomFont.customFontRegular)
                    }

                    4 -> {
                        Text(text = "나흘 전", fontFamily = CustomFont.customFontRegular)
                    }

                    else -> {
                        Text(
                            text = "${myReviewViewModel.calculationDate(review.reviewTimeStamp)} 일전",
                            fontFamily = CustomFont.customFontRegular
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                if (!myReviewViewModel.menuStateMap[pos]!!) {
                    CustomIconButton(iconButtonOnClick = {
                        myReviewViewModel.onClickIconMenu(pos)
                    }, icon = Icons.Default.MoreVert)
                } else {
                    Row {
                        // 수정
                        CustomIconButton(
                            iconButtonOnClick = {
                                Log.d("test100","cID : ${review.contentId}")
                                Log.d("test100","rDI : ${review.reviewDocId}")
                                myReviewViewModel.onClickIconEditReview(review.contentId,review.reviewDocId)

                            },
                            icon = ImageVector.vectorResource(R.drawable.ic_edit_24px)
                        )

                        // 삭제
                        CustomIconButton(
                            iconButtonOnClick = {
                                myReviewViewModel.onClickIconDeleteReview(review)
                            },
                            icon = ImageVector.vectorResource(R.drawable.ic_delete_24px)
                        )
                    }
                }
            }

        }
    }
}
