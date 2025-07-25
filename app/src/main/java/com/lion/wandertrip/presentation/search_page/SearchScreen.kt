package com.lion.wandertrip.presentation.search_page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.component.HomeSearchBar
import com.lion.wandertrip.presentation.search_page.component.RecentItem

@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {

    Scaffold(
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 🔍 수정된 검색 바 적용
            HomeSearchBar(
                query = viewModel.searchQuery,
                onSearchQueryChanged = { viewModel.updateQuery(it) },
                onSearchClicked = {
                    if (viewModel.searchQuery.isNotBlank()) {
                        viewModel.addSearchToRecent(viewModel.searchQuery)
                        viewModel.onClickToResult(viewModel.searchQuery)
                    }
                },
                onClearQuery = { viewModel.updateQuery("") },
                onBackClicked = { viewModel.backScreen() }
            )

            // ✅ 최근 검색어 목록 추가
            RecentItem(searchViewModel = viewModel)
        }
    }
}


