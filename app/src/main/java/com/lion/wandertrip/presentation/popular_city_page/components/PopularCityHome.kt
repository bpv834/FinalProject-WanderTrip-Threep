package com.lion.wandertrip.presentation.popular_city_page.components

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel

@Composable
fun PopularCityHome(
    lat: Double,
    lng: Double,
    cityName: String,
    viewModel: PopularCityViewModel = hiltViewModel()
) {

}