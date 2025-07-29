package com.lion.wandertrip.presentation.my_interesting_page

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.UserInterestingModel
import com.lion.wandertrip.service.ContentsService
import com.lion.wandertrip.service.TripCommonItemService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.ContentTypeId
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.Tools
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyInterestingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userService: UserService,
    private val tripCommonItemService: TripCommonItemService,
    private val contentsService: ContentsService,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val tripApplication = context as TripApplication
    // 로딩 상태변수
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // 카테고리 관리 상태변수
    private val _filteredCityName = MutableStateFlow("전체")
    val filteredCityName: StateFlow<String> = _filteredCityName.asStateFlow()
    // 관광지 선택여부
    private val _isCheckAttraction = MutableStateFlow(true)
    // 식당 선택여부
    private val _isCheckRestaurant = MutableStateFlow(true)
    // 숙소 선택여부
    private val _isCheckAccommodation = MutableStateFlow(true)

    // 관심목록 아이템 리스트 (아이템 정보/ 컨텐츠 정보 결합)
    private val _interestingListAll = MutableStateFlow<List<UserInterestingModel>>(emptyList())
    // 리스트 가져오기
    // 인기관광지 페이지는 컨텐츠맵을 실시간으로 받아서 사용함
    // 하지만 내 관심 페이지는 외부에서 데이터 변경되는걸 반영하는게 중요한게 아님 좋아요수 별점
    fun getInterestingList() {
        Log.d("init","getInterestingList 시작")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 관심 컨텐트 아이디 리스트
                val contentIdList = userService.gettingUserInterestingContentIdList(tripApplication.loginUserModel.userDocId)
                val modelList = tripCommonItemService.gettingTripItemCommonInteresting(contentIdList.toMutableList())

                modelList.forEach { item ->
                    val content = contentsService.getContentByContentsId(item.contentID)
                    item.ratingScore = content.ratingScore
                    item.starRatingCount = content.getRatingCount
                    item.saveCount = content.interestingCount
                }

                _interestingListAll.value = modelList
                updateLocalList(modelList)

                // 좋아요 맵 초기화
                _likeMap.value = modelList.associate { it.contentID to true }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 아이템들의 지역 목록
    private val _localList = MutableStateFlow<List<String>>(emptyList())
    val localList: StateFlow<List<String>> = _localList.asStateFlow()
    // 좋아요여부 <ContentId,Bool>
    private val _likeMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val likeMap: StateFlow<Map<String, Boolean>> = _likeMap.asStateFlow()
    // 카테고리 시트 오픈 여부
    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen: StateFlow<Boolean> = _isSheetOpen.asStateFlow()

    // 바텀시트 여는 메서드
    fun onClickCityDropdown() {
        _isSheetOpen.value = true
    }

    // 바텀시트 닫는 메서드
    fun closeBottomSheet() {
        _isSheetOpen.value = false
    }
    // 카테고리 선택 여부
    val isCheckAttraction: StateFlow<Boolean> = _isCheckAttraction.asStateFlow()
    val isCheckRestaurant: StateFlow<Boolean> = _isCheckRestaurant.asStateFlow()
    val isCheckAccommodation: StateFlow<Boolean> = _isCheckAccommodation.asStateFlow()


    val interestingListFiltered: StateFlow<List<UserInterestingModel>> =
        combine(
            _interestingListAll,
            _filteredCityName,
            _isCheckAttraction,
            _isCheckRestaurant,
            _isCheckAccommodation
        ) { allList, cityName, checkAttraction, checkRestaurant, checkAccommodation ->
            // 지역필터
            val filteredByCity = if (cityName == "전체") {
                allList
            } else {
                allList.filter {
                    Tools.getAreaDetails(it.areacode, it.sigungucode) == cityName
                }
            }
            // 카테고리필터
            val filteredByCategory = filteredByCity.filter { item ->
                val type = item.contentTypeID
                val isAttraction = type == ContentTypeId.TOURIST_ATTRACTION.contentTypeCode.toString()
                val isRestaurant = type == ContentTypeId.RESTAURANT.contentTypeCode.toString()
                val isAccommodation = type == ContentTypeId.ACCOMMODATION.contentTypeCode.toString()

                (isAttraction && checkAttraction) ||
                        (isRestaurant && checkRestaurant) ||
                        (isAccommodation && checkAccommodation)
            }
            // 필터된 리스트 리턴
            filteredByCategory
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun updateLocalList(list: List<UserInterestingModel>) {
        val cities = list.map {
            Tools.getAreaDetails(it.areacode, it.sigungucode)
        }.distinct().sorted().toMutableList()
        cities.add(0, "전체")
        _localList.value = cities
    }

    // 좋아요 클릭 리스너
    fun onClickIconHeart(contentId: String) {
        val current = _likeMap.value.toMutableMap()
        val isLiked = !(current[contentId] ?: false)
        current[contentId] = isLiked
        _likeMap.value = current

        viewModelScope.launch {
            if (isLiked) {
                // 무관심->관심
                userService.addLikeItem(tripApplication.loginUserModel.userDocId, contentId)
                userService.addLikeCnt(contentId)
            } else {
                // 관심->무관심
                userService.removeLikeItem(tripApplication.loginUserModel.userDocId, contentId)
                userService.removeLikeCnt(contentId)
                _interestingListAll.value = _interestingListAll.value.filter { it.contentID != contentId }
            }
        }
    }
    // 필터 문자열 관리
    fun setFilteredCity(city: String) {
        _filteredCityName.value = city
    }
    // 관광지 토글 리스너
    fun toggleAttraction() {
        _isCheckAttraction.value = !_isCheckAttraction.value
    }
    // 식당 토글 리스너
    fun toggleRestaurant() {
        _isCheckRestaurant.value = !_isCheckRestaurant.value
    }
    // 숙소 토글 리스너
    fun toggleAccommodation() {
        _isCheckAccommodation.value = !_isCheckAccommodation.value
    }
    // 아이템 클릭리스너 -> 상세화면으로
    fun onClickListItemToDetailScreen(contentId: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL}/$contentId")
    }
    // 뒤로가기 리스너
    fun onClickNavIconBack() {
        tripApplication.navHostController.popBackStack()
    }
    // 필터 시트 관리 메서드
    fun setSheetOpen(open: Boolean) {
        _isSheetOpen.value = open
    }

    init {
        getInterestingList()
    }

}
