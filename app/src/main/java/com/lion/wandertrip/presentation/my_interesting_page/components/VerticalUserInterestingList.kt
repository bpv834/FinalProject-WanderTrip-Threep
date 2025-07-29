package com.lion.wandertrip.presentation.my_interesting_page.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lion.wandertrip.R
import com.lion.wandertrip.component.CustomRatingBar
import com.lion.wandertrip.model.UserInterestingModel
import com.lion.wandertrip.presentation.my_interesting_page.MyInterestingViewModel
import com.lion.wandertrip.ui.theme.Gray0
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.util.CustomFont
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage
@Composable
fun VerticalUserInterestingList(
    likeMap: Map<String, Boolean>,
    viewModel: MyInterestingViewModel,
    items: List<UserInterestingModel>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(items) { item ->
            UserInterestingItem(
                viewModel = viewModel,
                interestingItem = item,
                isLike = likeMap[item.contentID] ?: false
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun UserInterestingItem(
    viewModel: MyInterestingViewModel,
    interestingItem: UserInterestingModel,
    isLike: Boolean
) {
    val contentId = interestingItem.contentID

    Row(
        modifier = Modifier
            .clickable {
                viewModel.onClickListItemToDetailScreen(contentId)
            }
            .fillMaxWidth()
            .background(Gray0, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이미지
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
        ) {
            GlideImage(
                imageModel = interestingItem.smallImagePath,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                circularReveal = CircularReveal(duration = 300),
                placeHolder = ImageBitmap.imageResource(R.drawable.img_image_holder),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 텍스트 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = interestingItem.contentTitle ?: "제목 없음",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CustomFont.customFontBold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = (interestingItem.addr1 + interestingItem.addr2) ?: "주소 없음",
                fontSize = 14.sp,
                fontFamily = NanumSquareRoundRegular,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomRatingBar(rating = interestingItem.ratingScore)
        }

        // 좋아요 아이콘
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    viewModel.onClickIconHeart(contentId)
                },
            contentAlignment = Alignment.Center
        ) {
            val vector = if (isLike) {
                R.drawable.ic_heart_filled_24px
            } else {
                R.drawable.ic_heart_empty_24px
            }

            Icon(
                imageVector = ImageVector.vectorResource(vector),
                contentDescription = "Like",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}