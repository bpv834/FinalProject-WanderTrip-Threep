package com.lion.wandertrip.presentation.bottom.home_page.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.ui.theme.NanumSquareRound

@Composable
fun TravelSpotItem(
    tripItem: TripItemModel,
    onItemClick: (TripItemModel) -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

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
                painterResource(R.drawable.ic_hide_image_144dp) // ✅ 기본 이미지 적용
            } else {
                rememberAsyncImagePainter(tripItem.firstImage)
            }

            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp), // 아이콘이 너무 붙지 않도록 패딩 추가
                contentAlignment = Alignment.TopEnd // ✅ 우측 하단 정렬
            ) {
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier.size(24.dp) // 적절한 크기 설정
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
            Text(tripItem.cat1, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

