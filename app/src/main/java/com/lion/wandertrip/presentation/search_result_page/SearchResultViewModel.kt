package com.lion.wandertrip.presentation.search_result_page

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.service.TripAreaBaseItem2Service
import com.lion.wandertrip.service.TripKeywordItemService
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.Tools
import com.lion.wandertrip.util.TripNoteScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val tripNoteService: TripNoteService,
    val tripAreaBaseItem2Service: TripAreaBaseItem2Service
): ViewModel() {

    val tripApplication = context as TripApplication

    // TripItemModel 검색 결과
    private val _searchResults = MutableStateFlow<List<TripItemModel>>(emptyList())
    val searchResults: StateFlow<List<TripItemModel>> get() = _searchResults

    // TripNoteModel 검색 결과
    private val _searchNoteResults = MutableStateFlow<List<TripNoteModel>>(emptyList())
    val searchNoteResults: StateFlow<List<TripNoteModel>> get() = _searchNoteResults

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    // 검색 메서드
    fun searchTrip(keyword: String) {
        viewModelScope.launch {
            _isLoading.value = true // ✅ 검색 시작 전 로딩 시작
            val (areaCode,sigunguCode) = Tools.getAreaAndSubAreaCode(keyword)?:Pair("","")
            val work1 = async(Dispatchers.IO){
                tripAreaBaseItem2Service.gettingAllItemWithAreaCode(areaCode,sigunguCode)
            }
            val work2 = async(Dispatchers.IO){
                tripNoteService.gettingTripNoteList()
            }
            _searchResults.value =work1.await()?: emptyList()
            _searchNoteResults.value = work2.await()

            _isLoading.value = false
        }
    }

    fun onNavigateDetail(contentId: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL.name}/$contentId")
    }

    fun onNavigateTripNote(documentId: String) {
        tripApplication.navHostController.navigate("${TripNoteScreenName.TRIP_NOTE_DETAIL.name}/$documentId")
    }

    fun onNavigateBackToSearchScreen() {
        tripApplication.navHostController.popBackStack(
            route = MainScreenName.MAIN_SCREEN_SEARCH.name, // ✅ 검색창 화면으로 이동
            inclusive = false // ✅ 기존 검색창을 새로 생성하도록 설정
        )
    }

}