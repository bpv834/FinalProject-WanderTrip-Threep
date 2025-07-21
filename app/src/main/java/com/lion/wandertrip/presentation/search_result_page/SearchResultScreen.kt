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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
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
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.presentation.search_page.SearchViewModel
import com.lion.wandertrip.presentation.search_page.component.HomeSearchBar
import com.lion.wandertrip.presentation.search_result_page.component.MoreButton
import com.lion.wandertrip.presentation.search_result_page.component.SearchItem
import com.lion.wandertrip.presentation.search_result_page.component.SearchTripNoteItem
import com.lion.wandertrip.util.ContentTypeId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    contentId: String,
    viewModel: SearchResultViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    var searchInput by remember { mutableStateOf(contentId) }
    var searchQuery by remember { mutableStateOf(contentId) }

    var selectedCategoryCode by remember { mutableStateOf<String?>("추천") }

    val isLoading by viewModel.isLoading.collectAsState() // ✅ 로딩 상태 감지

    // ✅ ViewModel에서 검색 결과 가져오기
    val filteredList by viewModel.searchResults.collectAsState()
    val filteredNoteList by viewModel.searchNoteResults.collectAsState()

    // ✅ "더보기"를 눌렀을 때 표시할 개수를 저장하는 Map
    val visibleItemCounts = remember { mutableStateMapOf<String, Int>() }

    // ✅ 검색어 변경 시 검색 실행
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.searchTrip(searchQuery)
        }
    }

    fun getCategoryName(contentTypeId: String?): String {
        return when (contentTypeId) {
            ContentTypeId.TOURIST_ATTRACTION.contentTypeCode.toString() -> "관광지"
            ContentTypeId.ACCOMMODATION.contentTypeCode.toString() -> "숙소"
            ContentTypeId.RESTAURANT.contentTypeCode.toString() -> "맛집"
            else -> "기타"
        }
    }

    val categorizedResults = when (selectedCategoryCode) {
        "관광지" -> filteredList.filter { getCategoryName(it.contentTypeId) == "관광지" }
            .groupBy { getCategoryName(it.contentTypeId) }

        "숙소" -> filteredList.filter { getCategoryName(it.contentTypeId) == "숙소" }
            .groupBy { getCategoryName(it.contentTypeId) }

        "맛집" -> filteredList.filter { getCategoryName(it.contentTypeId) == "맛집" }
            .groupBy { getCategoryName(it.contentTypeId) }

        else -> filteredList.groupBy { getCategoryName(it.contentTypeId) } // ✅ "추천" 또는 null일 경우 모든 데이터 표시
    }

// ✅ 여행기 데이터는 별도로 관리
    val categorizedNoteResults =
        if (selectedCategoryCode == "여행기" || selectedCategoryCode == "추천") {
            filteredNoteList
        } else {
            emptyList()
        }


    Scaffold(containerColor = Color.White) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // ✅ 검색 바
            HomeSearchBar(
                query = searchInput,
                onSearchQueryChanged = { newValue -> searchInput = newValue },
                onSearchClicked = {
                    if (searchInput.isNotBlank()) {
                        searchQuery = searchInput
                        val searchItem = TripItemModel(title = searchInput)
                        searchViewModel.addSearchToRecent(searchItem)
                        searchViewModel.onClickToResult(searchInput)
                    }
                },
                onClearQuery = { searchInput = "" },
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
                        val visibleCount = visibleItemCounts[category] ?: 3

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
                            items(itemsForCategory.take(visibleCount)) { tripItem ->
                                SearchItem(
                                    tripItem = tripItem, // ✅ TripItemModel만 처리
                                    onItemClick = { viewModel.onNavigateDetail(tripItem.contentId) }
                                )
                                CustomDividerComponent(10.dp)
                            }
                        } else {
                            item { NoResultsMessage(category) }
                        }

                        if (visibleCount < itemsForCategory.size) {
                            item {
                                MoreButton(category = category) {
                                    visibleItemCounts[category] = visibleCount + 3
                                }
                            }
                        }

                        if (selectedCategoryCode == "추천" && category != requiredCategories.last()) {
                            item { CustomDividerComponent(16.dp) }
                        }
                    }

                    // ✅ 여행기 데이터를 별도로 처리
                    if (selectedCategoryCode == "여행기" || selectedCategoryCode == "추천") {
                        val visibleCount = visibleItemCounts["여행기"] ?: 3

                        item {
                            Text(
                                text = "여행기",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (categorizedNoteResults.isNotEmpty()) {
                            items(categorizedNoteResults.take(visibleCount)) { tripNote ->
                                SearchTripNoteItem( // ✅ TripNoteModel을 처리할 Compose 함수 사용
                                    tripNote = tripNote,
                                    onItemClick = { viewModel.onNavigateTripNote(tripNote.tripNoteDocumentId) }
                                )
                                CustomDividerComponent(10.dp)
                            }
                        } else {
                            item { NoResultsMessage("여행기") }
                        }

                        if (visibleCount < categorizedNoteResults.size) {
                            item {
                                MoreButton(category = "여행기") {
                                    visibleItemCounts["여행기"] = visibleCount + 3
                                }
                            }
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