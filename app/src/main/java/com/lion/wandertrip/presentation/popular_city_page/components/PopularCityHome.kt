package com.lion.wandertrip.presentation.popular_city_page.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lion.wandertrip.presentation.my_trip_page.MyTripViewModel
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel

@Composable
fun PopularCityHome(
    lat: Double,
    lng: Double,
    cityName: String,
    radius : String,
    viewModel: PopularCityViewModel
) {


    LaunchedEffect (Unit){

    }
    LazyColumn  {


    }

}