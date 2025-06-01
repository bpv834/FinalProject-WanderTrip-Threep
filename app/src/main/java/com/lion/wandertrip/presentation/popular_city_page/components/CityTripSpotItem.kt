package com.lion.wandertrip.presentation.popular_city_page.components



import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.lion.wandertrip.R
import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.model.UnifiedSpotItem
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel
import com.lion.wandertrip.util.Tools
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage


@Composable
fun CityTripSpotItem(
    unifiedSpotItem: UnifiedSpotItem,
    onFavoriteClick: (String) -> Unit,
    viewModel: PopularCityViewModel,
    isFavorite: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            GlideImage(
                imageModel = unifiedSpotItem.publicData.firstImage,
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
                        onFavoriteClick(unifiedSpotItem.publicData.contentId?:"")
                    }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = unifiedSpotItem.publicData.title ?: "관광지 이름",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = Tools.getAreaDetails(unifiedSpotItem.publicData.areaCode?:"" ,unifiedSpotItem.publicData.siGunGuCode) ?: "위치",
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
                    text = "${unifiedSpotItem.privateData?.ratingScore}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )

                Text(
                    text = "${"${unifiedSpotItem.privateData?.getRatingCount ?: 0}"}명",
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
                    text = "${unifiedSpotItem.privateData?.interestingCount ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
