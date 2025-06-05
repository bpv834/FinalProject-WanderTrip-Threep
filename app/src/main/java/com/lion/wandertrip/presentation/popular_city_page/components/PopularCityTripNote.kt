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
import com.lion.wandertrip.presentation.bottom.home_page.components.PopularTripNoteItem
import com.lion.wandertrip.presentation.my_trip_note.components.TripNoteItem
import com.lion.wandertrip.presentation.popular_city_page.PopularCityViewModel
import com.lion.wandertrip.util.CustomFont
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PopularCityTripNote(
    viewModel: PopularCityViewModel
) {
    val tripList = viewModel.tripNoteList
    val listState = rememberLazyListState() // 스크롤 상태

    // 마지막 아이템에 도달했는지 판단
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem?.index == totalItems - 1
        }
    }

    // 마지막 도달 시 페이지 요청
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd) {
                    /*Log.d("PopularCityTripNote", "스크롤 마지막 도달 → 다음 페이지 요청")
                    viewModel.nextAttraction()*/
                }
            }
    }

    LazyColumn(state = listState) {
        item {
            Text(
                text = "여행기 목록 : ${viewModel.tripNoteList.size}",
                fontFamily = CustomFont.customFontBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(tripList.size) { index ->
            val item = tripList[index]
            PopularTripNoteItem(item, { viewModel.onClickTripNote(item.tripNoteDocumentId) })
        }
    }
}