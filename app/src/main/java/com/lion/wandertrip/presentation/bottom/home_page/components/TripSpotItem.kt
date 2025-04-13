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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
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
import com.lion.wandertrip.util.Tools
import com.lion.wandertrip.util.Tools.Companion.AreaCityMap
import com.lion.wandertrip.util.Tools.Companion.areaCodeMap
import com.lion.wandertrip.util.TripItemCat2
import com.lion.wandertrip.util.TripItemCat2.Companion.fromCodeTripItemCat2
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun TripSpotItem(
    tripItem: TripItemModel,
    userModel: UserModel,
    contentsModel: ContentsModel?,
    onItemClick: (TripItemModel) -> Unit,
    onFavoriteClick: (String) -> Unit,
    viewModel: HomeViewModel,
    isFavorite: Boolean,
) {
    Log.d(
        "test100",
        "contentId : ${contentsModel?.contentId} ratingScore : ${contentsModel?.ratingScore} getRatingCount : ${contentsModel?.getRatingCount} interestingCount : ${contentsModel?.interestingCount} "
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .clickable { onItemClick(tripItem) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            GlideImage(
                imageModel = tripItem.firstImage,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                circularReveal = CircularReveal(duration = 250),
                placeHolder = ImageBitmap.imageResource(R.drawable.img_image_holder),
            )
            val vector = when (isFavorite) {
                true -> R.drawable.ic_heart_filled_24px
                false -> R.drawable.ic_heart_empty_24px
            }
            Icon(
                imageVector = ImageVector.vectorResource(vector),
                contentDescription = "Save",
                tint = Color.Red,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clickable {
                        onFavoriteClick(tripItem.contentId)
                    }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = tripItem.title ?: "관광지 이름",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = Tools.getAreaDetails(tripItem.areaCode, tripItem.sigunguCode) ?: "위치",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107), // 노란 별색
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${contentsModel?.ratingScore ?: 0.0}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )

                Text(
                    text = "${"${contentsModel?.getRatingCount ?: 0}"}명",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "관심",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${contentsModel?.interestingCount ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
