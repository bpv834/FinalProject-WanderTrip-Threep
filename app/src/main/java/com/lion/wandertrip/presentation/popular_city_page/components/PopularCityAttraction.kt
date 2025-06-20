package com.lion.wandertrip.presentation.popular_city_page.components

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel
import com.lion.wandertrip.util.CustomFont
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PopularCityAttraction(
    viewModel: PopularCityViewModel
) {
    val attractionList by viewModel.attractionList.collectAsState()
    val listState = rememberLazyListState() // 스크롤 상태
    val userLikeList by viewModel.userLikeList.collectAsState()

    // 마지막 아이템에 도달했는지 판단
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() //listState.layoutInfo.visibleItemsInfo: 현재 화면에 보이는 아이템들 목록
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem?.index == totalItems - 1
        }
    }

    // 마지막 도달 시 페이지 요청
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value } // //  Compose의 State를 Flow로 감싸서 변화가 생길 때마다 emit합니다. // 즉, shouldLoadMore.value가 true/false로 바뀔 때마다 흘러갑니다.
            .distinctUntilChanged() //distinctUntilChanged(): 같은 값이 연속으로 나오면 무시 (예: true → true → true 방지)
            .collect { isAtEnd ->
                if (isAtEnd) {
                    Log.d("PopularCityAttraction", "스크롤 마지막 도달 → 다음 페이지 요청")
                    viewModel.nextAttraction()
                }
            }
    }

    LazyColumn(state = listState) {
        item {
            Text(
                text = "관광지 목록 : ${viewModel.totalAttractionCount.value}",
                fontFamily = CustomFont.customFontBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(attractionList.size) { index ->
            val item = attractionList[index]
            CityTripSpotItem(item, {viewModel.toggleFavorite(item.publicData.contentId?:"")}, viewModel, userLikeList.contains(item.publicData.contentId), { viewModel.onClickTrip(item.publicData.contentId?:"") })
        }
    }
}