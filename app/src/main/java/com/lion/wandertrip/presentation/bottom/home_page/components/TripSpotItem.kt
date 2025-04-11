package com.lion.wandertrip.presentation.bottom.home_page.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.lion.wandertrip.presentation.bottom.home_page.HomeViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.model.content.ContentModel
import com.lion.wandertrip.R
import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.util.AccommodationItemCat3
import com.lion.wandertrip.util.AccommodationItemCat3.Companion.fromCodeAccommodationItemCat3
import com.lion.wandertrip.util.AreaCode
import com.lion.wandertrip.util.RestaurantItemCat3
import com.lion.wandertrip.util.RestaurantItemCat3.Companion.fromCodeRestaurantItemCat3
import com.lion.wandertrip.util.Tools.Companion.AreaCityMap
import com.lion.wandertrip.util.Tools.Companion.areaCodeMap
import com.lion.wandertrip.util.TripItemCat2
import com.lion.wandertrip.util.TripItemCat2.Companion.fromCodeTripItemCat2

@Composable
fun TripSpotItem(
    tripItem: TripItemModel,
    userModel: UserModel,
    contentsModel: ContentsModel?,
    onItemClick: (TripItemModel) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    // ✅ userLikeList가 변경될 때 자동 업데이트
    val isFavorite by remember(userModel.userLikeList) {
        derivedStateOf { userModel.userLikeList.contains(tripItem.contentId) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onItemClick(tripItem) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(60.dp)) {
            val imagePainter = if (tripItem.firstImage.isNullOrBlank()) {
                painterResource(R.drawable.ic_hide_image_144dp)
            } else {
                rememberAsyncImagePainter(tripItem.firstImage)
            }

           /* Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )*/

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { onFavoriteClick(tripItem.contentId) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.LightGray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(tripItem.title, fontSize = 14.sp, fontFamily = NanumSquareRound)
            val areaMap = areaCodeMap[tripItem.areaCode]
            val contentsCategory = when (tripItem.contentTypeId.toInt()) {
                12 -> TripItemCat2.fromCodeTripItemCat2(tripItem.cat2)?.catName ?: "기타"
                32 -> AccommodationItemCat3.fromCodeAccommodationItemCat3(tripItem.cat3)?.catName ?: "기타"
                39 -> RestaurantItemCat3.fromCodeRestaurantItemCat3(tripItem.cat3)?.catName ?: "기타"
                else -> "기타"
            }
            Log.d("contentCategoryCheck","${tripItem.contentTypeId},${tripItem.cat2}, ${tripItem.cat3}, $contentsCategory")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(areaMap.toString(), fontSize = 12.sp, color = Color.Gray)

                Text("/", fontSize = 12.sp, color = Color.Gray) // ✅ 구분자 추가

                Text(contentsCategory, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(4.dp)) // ✅ 간격 추가

            // ⭐ 별점 및 리뷰 개수를 한 줄에 정렬
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star_24px),
                    contentDescription = "rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp) // ✅ 아이콘 크기 약간 키움
                )
                Text(
                    text = "${contentsModel?.ratingScore ?: 0} (${contentsModel?.getRatingCount ?: 0})",
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp)
                )

                // ❤️ 좋아요 개수를 한 줄에 정렬
                Icon(
                    painter = painterResource(id = R.drawable.ic_heart_red),
                    contentDescription = "likes",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${contentsModel?.interestingCount ?: 0}",
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}



