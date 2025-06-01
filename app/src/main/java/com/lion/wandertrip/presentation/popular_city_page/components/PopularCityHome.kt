package com.lion.wandertrip.presentation.popular_city_page.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.presentation.my_trip_page.MyTripViewModel
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel

@Composable
fun PopularCityHome(
    viewModel: PopularCityViewModel
) {
    val restaurantList by viewModel.restaurantList.collectAsState()
    val attractionList by viewModel.attractionList.collectAsState()
    val accommodationList by viewModel.accommodationList.collectAsState()

    LaunchedEffect (Unit){

    }
    LazyColumn  {
        items(attractionList){item->
            CityTripSpotItem(
                item,{},viewModel,true
            )
        }
        item{
            CustomDividerComponent(10.dp)
        }
        items(restaurantList){item->
            CityTripSpotItem(
                item,{},viewModel,true
            )
        }
        item{
            CustomDividerComponent(10.dp)
        }
        items(accommodationList){item->
            CityTripSpotItem(
                item,{},viewModel,true
            )
        }
        item{
            CustomDividerComponent(10.dp)
        }
    }

}