package com.lion.wandertrip.presentation.popular_city_page.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel
import com.lion.wandertrip.util.CustomFont

@Composable
fun LazyRowPageTap(viewModel: PopularCityViewModel) {
    val popularCityState by viewModel.tapViewFlow.collectAsState()
    val tabs = listOf("홈", "관광지", "식당", "숙소", "여행기")
    val selectedTabIndex = popularCityState // 또는 remember { mutableStateOf(0) }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items(tabs.size) { index ->
            val title = tabs[index]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (selectedTabIndex == index) Color(0xFF1976D2) else Color.White
                    )
                    .border(
                        width = 1.dp,
                        color = if (selectedTabIndex == index) Color(0xFF1976D2) else Color.Gray,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { viewModel.changeState(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    color = if (selectedTabIndex == index) Color.White else Color.Black,
                    fontSize = 14.sp,
                    fontFamily = if (selectedTabIndex == index)
                        CustomFont.customFontBold else CustomFont.customFontRegular
                )
            }
        }
    }

}