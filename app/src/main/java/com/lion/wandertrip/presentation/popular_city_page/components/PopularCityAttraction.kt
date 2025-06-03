package com.lion.wandertrip.presentation.popular_city_page.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel
import com.lion.wandertrip.util.CustomFont


@Composable
fun PopularCityAttraction(
    viewModel: PopularCityViewModel
) {
    val attractionList by viewModel.attractionList.collectAsState()


    LazyColumn {
        item{
            Text("다음", modifier = Modifier.clickable {
                viewModel.nextAttraction()
            })
        }
        item {
            Text(
                text = "관광지 목록 : ${viewModel.totalAttractionCount.value}",
                fontFamily = CustomFont.customFontBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 6.dp)

            )
        }


        item{
            Column {
                attractionList.forEach { item ->
                    CityTripSpotItem(item, {}, viewModel, true)
                }
            }
        }
    }
}