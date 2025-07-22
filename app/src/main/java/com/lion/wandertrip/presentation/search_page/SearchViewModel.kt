package com.lion.wandertrip.presentation.search_page

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.util.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripScheduleService: TripScheduleService
) : ViewModel() {
    val tripApplication = context as TripApplication

    val application = context as TripApplication

    fun backScreen() {
        application.navHostController.popBackStack()
    }

    // 검색어 상태를 ViewModel에 저장 (상태 호이스팅)
    var searchQuery by mutableStateOf("")
        private set

    fun updateQuery(newQuery: String) {
        searchQuery = newQuery
    }


    val recentSearches = mutableStateListOf<String>() // 최근 검색어 리스트

    fun clearRecentSearches() {
        recentSearches.clear()
    }

    fun selectRecentSearch(query: String) {
        searchQuery = query // ✅ 검색어 상태 업데이트
        onClickToResult(searchQuery) // ✅ 검색 실행
    }


    fun removeRecentSearch(removeKeyword: String) {
        recentSearches.remove(removeKeyword)
    }

    fun addSearchToRecent(addKeyword: String) {
        // 중복 검색어가 있는지 확인하고 제거 (최신 검색어를 맨 앞으로 유지)
        recentSearches.removeAll { it == addKeyword }

        // 새로운 검색어를 리스트의 맨 앞에 추가
        recentSearches.add(0, addKeyword)

        // 최근 검색어 개수를 제한 (예: 10개까지만 저장)
        if (recentSearches.size > 5) {
            recentSearches.removeAt(recentSearches.lastIndex)
        }
    }

    fun onClickToResult(query : String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_SEARCH_RESULT.name}/${query}")
    }
}
