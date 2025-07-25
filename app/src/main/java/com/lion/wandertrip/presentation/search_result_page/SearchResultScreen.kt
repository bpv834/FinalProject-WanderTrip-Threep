package com.lion.wandertrip.presentation.search_result_page

import SearchItemCategoryChips
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.search_page.SearchViewModel
import com.lion.wandertrip.component.HomeSearchBar
import com.lion.wandertrip.presentation.search_result_page.component.SearchItem
import com.lion.wandertrip.presentation.search_result_page.component.SearchTripNoteItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    viewModel: SearchResultViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    var selectedCategoryCode by remember { mutableStateOf<String?>("관광지") }

    val isLoading by viewModel.isLoading.collectAsState() // ✅ 로딩 상태 감지


    val searchQuery by viewModel.searchQuery.collectAsState()
    val categorizedResults by viewModel.categorizedResults.collectAsState()
    val searchNoteResult by viewModel.searchNoteResults.collectAsState()

    Scaffold(containerColor = Color.White) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // ✅ 검색 바
            HomeSearchBar(
                query = searchQuery,
                onSearchQueryChanged = { viewModel.queryChange(newQuery = it) },
                onSearchClicked = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchTrip(searchQuery)
                        searchViewModel.addSearchToRecent(searchQuery)
                        searchViewModel.onClickToResult(searchQuery)
                    }
                },
                onClearQuery = { viewModel.queryChange("") },
                onBackClicked = { viewModel.onNavigateBackToSearchScreen() }
            )

            // ✅ 카테고리 필터
            SearchItemCategoryChips(
                selectedCategoryCode = selectedCategoryCode,
                onCategorySelected = { selectedCategoryCode = it }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieLoadingIndicator() // ✅ 로딩 UI
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val requiredCategories = listOf("관광지", "숙소", "맛집")

                    requiredCategories.forEach { category ->
                        val itemsForCategory = categorizedResults[category] ?: emptyList()

                        if (selectedCategoryCode != "추천" && selectedCategoryCode != category) {
                            return@forEach
                        }

                        item {
                            Text(
                                text = category,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (itemsForCategory.isNotEmpty()) {
                            items(itemsForCategory) { tripItem ->
                                SearchItem(
                                    tripItem = tripItem, // ✅ TripItemModel만 처리
                                    onItemClick = { viewModel.onNavigateDetail(tripItem.contentId) }
                                )
                                CustomDividerComponent(10.dp)
                            }
                        } else {
                            item { NoResultsMessage(category) }
                        }
                    }

                    // ✅ 여행기 데이터를 별도로 처리
                    if (selectedCategoryCode == "여행기") {

                        item {
                            Text(
                                text = "여행기",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (searchNoteResult.isNotEmpty()) {
                            items(searchNoteResult) { tripNote ->
                                SearchTripNoteItem( // ✅ TripNoteModel을 처리할 Compose 함수 사용
                                    tripNote = tripNote,
                                    onItemClick = { viewModel.onNavigateTripNote(tripNote.tripNoteDocumentId) }
                                )
                                CustomDividerComponent(10.dp)
                            }
                        } else {
                            item { NoResultsMessage("여행기") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoResultsMessage(category: String) {
    val message = when (category) {
        "맛집" -> "맛집이 없습니다."
        "여행기" -> "여행기가 없습니다."
        "관광지" -> "관광지가 없습니다."
        "숙소" -> "숙소가 없습니다."
        else -> "결과가 없습니다."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}