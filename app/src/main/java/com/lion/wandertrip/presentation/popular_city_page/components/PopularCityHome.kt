package com.lion.wandertrip.presentation.popular_city_page.components


import androidx.compose.foundation.background
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
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.presentation.bottom.home_page.components.PopularTripNoteItem
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel
import com.lion.wandertrip.util.CustomFont

@Composable
fun PopularCityHome(
    viewModel: PopularCityViewModel
) {

    val restaurantList by viewModel.restaurantListAtHome.collectAsState()
    val attractionList by viewModel.attractionListAtHome.collectAsState()
    val accommodationList by viewModel.accommodationListAtHome.collectAsState()
    val tripNoteList = viewModel.tripNoteList

    LazyColumn {
        if(tripNoteList.size!=0)
        item {
            Text(
                text = "추천 여행기",
                fontFamily = CustomFont.customFontBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // 최대 10개 제한
        items(minOf(tripNoteList.size, 10)) { index ->
            val item = tripNoteList[index]
            PopularTripNoteItem(item, {viewModel.onClickTripNote(item.tripNoteDocumentId)})
        }


        item {
            Text(
                text = "추천 관광지",
                fontFamily = CustomFont.customFontBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 6.dp)

            )
        }


        item {
            SwipeablePageContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                onSwipeLeft = { viewModel.loadNextAttractionPageAtHome() },
                onSwipeRight = { viewModel.loadPreAttractionPageAtHome() },
                content = {
                    Column {
                        attractionList.forEach { item ->
                            CityTripSpotItem(item, {viewModel.toggleFavorite(item.publicData.contentId?:"")}, viewModel, viewModel.userLikeList.value.contains(item.publicData.contentId), { viewModel.onClickTrip(item.publicData.contentId?:"") })
                        }
                    }
                }
            )
        }

        item {
            CustomDividerComponent(10.dp)
        }
        item {
            Text(
                text = "추천 식당",
                fontFamily = CustomFont.customFontBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }



        item {
            SwipeablePageContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                onSwipeLeft = { viewModel.loadNextRestaurantPageAtHome() },
                onSwipeRight = { viewModel.loadPreRestaurantPageAtHome() },
                content = {
                    Column {
                        restaurantList.forEach { item ->
                            CityTripSpotItem(item, {viewModel.toggleFavorite(item.publicData.contentId?:"")}, viewModel, viewModel.userLikeList.value.contains(item.publicData.contentId), { viewModel.onClickTrip(item.publicData.contentId?:"") })
                        }
                    }
                }
            )

        }

        item {
            CustomDividerComponent(10.dp)
        }
        item {
            Text(
                text = "추천 숙소",
                fontFamily = CustomFont.customFontBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
       item{
           SwipeablePageContainer(
               modifier = Modifier
                   .fillMaxWidth()
                   .background(Color.White)
                   .padding(8.dp),
               onSwipeLeft = { viewModel.loadNextAccommodationPageAtHome() },
               onSwipeRight = { viewModel.loadPreAccommodationPageAtHome() },
               content = {
                   Column {
                       accommodationList.forEach { item ->
                           CityTripSpotItem(item, {viewModel.toggleFavorite(item.publicData.contentId?:"")}, viewModel, viewModel.userLikeList.value.contains(item.publicData.contentId), { viewModel.onClickTrip(item.publicData.contentId?:"") })
                       }
                   }
               }
           )

       }
    }

}