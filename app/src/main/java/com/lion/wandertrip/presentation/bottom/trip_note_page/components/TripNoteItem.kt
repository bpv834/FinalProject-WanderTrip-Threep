package com.lion.wandertrip.presentation.bottom.trip_note_page.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun TripNoteItem(
    tripItem: TripNoteModel,
    onItemClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 여행기 제목
            Text(
                text = tripItem.tripNoteTitle,
                fontSize = 20.sp,
                fontFamily = NanumSquareRound,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 닉네임 . 1박2일
            Text(
                text = "${tripItem.userNickname} 님의 일정",
                fontSize = 14.sp,
                color = Color.Gray,
                fontFamily = NanumSquareRound
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 이미지 Glide
            if (tripItem.tripNoteImage.firstOrNull() != null) {
                GlideImage(
                    imageModel = tripItem.tripNoteImage.first(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    circularReveal = CircularReveal(duration = 250),
                    placeHolder = ImageBitmap.imageResource(R.drawable.img_image_holder),
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_hide_image_144dp),
                    contentDescription = "이미지 없음",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 내용 (최대 3줄, 넘치면 ...)
            Text(
                text = tripItem.tripNoteContent,
                fontSize = 16.sp,
                fontFamily = NanumSquareRound,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )
        }
    }
}