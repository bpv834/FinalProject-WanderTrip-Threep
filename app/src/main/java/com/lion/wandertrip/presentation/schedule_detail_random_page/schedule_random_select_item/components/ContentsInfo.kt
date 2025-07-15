package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.ScheduleRandomSelectItemViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.ui.theme.starColor

@Composable
fun ContentsInfo(viewModel : ScheduleRandomSelectItemViewModel,tripLocationBasedItem: TripLocationBasedItem){
    val contentsMap by viewModel.allContentsMapFlow.collectAsState()
    Column {
        // ✅ 제목 텍스트
        Text(
            text = tripLocationBasedItem.title!!,
            fontSize = 20.sp,
            fontFamily = NanumSquareRound,
            maxLines = 1, // ✅ 최대 1줄까지 표시
            overflow = TextOverflow.Ellipsis, // ✅ 너무 길면 "..." 표시
            modifier = Modifier.padding(bottom = 5.dp)
        )

        // ✅ 주소 텍스트
        Text(
            text = tripLocationBasedItem.addr1 + tripLocationBasedItem.addr2,
            fontSize = 12.sp,
            fontFamily = NanumSquareRoundRegular,
            maxLines = 1, // ✅ 최대 2줄까지 표시
            overflow = TextOverflow.Ellipsis, // ✅ 너무 길면 "..." 표시
            lineHeight = 14.sp // ✅ 줄 간격을 조정 (기본값보다 약간 좁게)
        )
        val contentModel =
            contentsMap[tripLocationBasedItem.contentId]
        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Row {
            Icon(
                painter = painterResource(R.drawable.ic_heart_red),
                tint = Color.Red,
                contentDescription = "관심 지역",
                modifier = Modifier.size(15.dp)
            )

            Spacer(
                modifier = Modifier.width(3.dp)
            )

            Text(
                text = "${contentModel?.interestingCount?:0}",
                fontSize = 15.sp,
                fontFamily = NanumSquareRoundRegular,
                lineHeight = 14.sp
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Icon(
                painter = painterResource(R.drawable.ic_star_full_24px), // 또는 다른 하트 아이콘
                contentDescription = "평점",
                tint = starColor,
                modifier = Modifier.size(15.dp)
            )

            Spacer(
                modifier = Modifier.width(3.dp)
            )

            Text(
                text = "${contentModel?.ratingScore?:0.0}",
                fontSize = 15.sp,
                fontFamily = NanumSquareRoundRegular,
                lineHeight = 14.sp
            )
        }
    }
}