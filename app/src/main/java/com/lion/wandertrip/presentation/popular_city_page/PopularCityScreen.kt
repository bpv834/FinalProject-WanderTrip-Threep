package com.lion.wandertrip.presentation.popular_city_page


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.R
import com.lion.wandertrip.presentation.popular_city_page.components.LazyRowPageTap
import com.lion.wandertrip.presentation.popular_city_page.components.PopularCityAccommodation
import com.lion.wandertrip.presentation.popular_city_page.components.PopularCityAttraction
import com.lion.wandertrip.presentation.popular_city_page.components.PopularCityHome
import com.lion.wandertrip.presentation.popular_city_page.components.PopularCityRestaurant
import com.lion.wandertrip.presentation.popular_city_page.components.PopularCityTripNote
import com.lion.wandertrip.util.PopularCityTap

@Composable
fun PopularCityScreen(
    /*    lat: String,
        lng: String,*/
    cityName: String,
    /*   radius : String,*/
    viewModel: PopularCityViewModel = hiltViewModel()
) {
    // 구독 설정
    val restaurantList by viewModel.restaurantListAtHome.collectAsState()
    val attractionList by viewModel.attractionListAtHome.collectAsState()
    val accommodationList by viewModel.accommodationListAtHome.collectAsState()
    val popularCityState by viewModel.tapViewFlow.collectAsState()
    val userLikeList by viewModel.userLikeList.collectAsState()
    LaunchedEffect(Unit) {
    }
    Scaffold(
        topBar = {
            CustomTopAppBar(
                navigationIconOnClick = {

                },
                navigationIconImage = ImageVector.vectorResource(R.drawable.ic_arrow_back_24px),
                title = cityName,
            )
        },

        ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 10.dp)
        ) {
            LazyRowPageTap(viewModel = viewModel)

            when (popularCityState) {
                PopularCityTap.POPULAR_CITY_TAP_HOME.num -> {
                    PopularCityHome(viewModel)
                }

                PopularCityTap.POPULAR_CITY_TAP_ATTRACTION.num -> {
                    PopularCityAttraction(viewModel)
                }

                PopularCityTap.POPULAR_CITY_TAP_RESTAURANT.num -> {
                    PopularCityRestaurant(viewModel)
                }

                PopularCityTap.POPULAR_CITY_TAP_ACCOMMODATION.num -> {
                    PopularCityAccommodation(viewModel)
                }

                PopularCityTap.POPULAR_CITY_TAP_TRIP_NOTE.num -> {
                    PopularCityTripNote(viewModel)
                }


            }
        }

    }
}