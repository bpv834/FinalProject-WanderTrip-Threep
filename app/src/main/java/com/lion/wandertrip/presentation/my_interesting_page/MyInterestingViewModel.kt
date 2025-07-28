package com.lion.wandertrip.presentation.my_interesting_page

import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyInterestingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userService: UserService,
    private val tripCommonItemService: TripCommonItemService,
    private val contentsService: ContentsService
) : ViewModel() {

    private val tripApp = context as TripApplication

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filteredCityName = MutableStateFlow("전체")
    val filteredCityName: StateFlow<String> = _filteredCityName.asStateFlow()

    private val _isCheckAttraction = MutableStateFlow(true)
    private val _isCheckRestaurant = MutableStateFlow(true)
    private val _isCheckAccommodation = MutableStateFlow(true)

    private val _interestingListAll = MutableStateFlow<List<UserInterestingModel>>(emptyList())
    private val _localList = MutableStateFlow<List<String>>(emptyList())
    val localList: StateFlow<List<String>> = _localList.asStateFlow()

    private val _likeMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val likeMap: StateFlow<Map<String, Boolean>> = _likeMap.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen: StateFlow<Boolean> = _isSheetOpen.asStateFlow()

    fun onClickCityDropdown() {
        _isSheetOpen.value = true
    }

    fun closeBottomSheet() {
        _isSheetOpen.value = false
    }

    fun onClickButtonAttraction() = toggleAttraction()
    fun onClickButtonRestaurant() = toggleRestaurant()
    fun onClickButtonAccommodation() = toggleAccommodation()

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
            val filtered = when (cityName) {
                "전체" -> allList
                else -> allList.filter {
                    Tools.getAreaDetails(it.areacode, it.sigungucode) == cityName
                }
            }.filter {
                val type = it.contentTypeID
                (checkAttraction || type != ContentTypeId.TOURIST_ATTRACTION.contentTypeCode.toString()) &&
                        (checkRestaurant || type != ContentTypeId.RESTAURANT.contentTypeCode.toString()) &&
                        (checkAccommodation || type != ContentTypeId.ACCOMMODATION.contentTypeCode.toString())
            }
            // likeMap 동기화
            _likeMap.value = filtered.associate { it.contentID to true }
            filtered
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getInterestingList() {
        viewModelScope.launch {
            _isLoading.value = true
            val contentIds = async(Dispatchers.IO) {
                userService.gettingUserInterestingContentIdList(tripApp.loginUserModel.userDocId)
            }.await()

            val userInterestList = async(Dispatchers.IO) {
                tripCommonItemService.gettingTripItemCommonInteresting(contentIds.toMutableList(), null)
            }.await()

            userInterestList.forEach {
                val contentModel = async(Dispatchers.IO) {
                    contentsService.getContentByContentsId(it.contentID)
                }.await()
                it.ratingScore = contentModel.ratingScore
                it.starRatingCount = contentModel.getRatingCount
                it.saveCount = contentModel.interestingCount
            }

            _interestingListAll.value = userInterestList
            updateLocalList(userInterestList)
            _isLoading.value = false
        }
    }

    private fun updateLocalList(list: List<UserInterestingModel>) {
        val cities = list.map {
            Tools.getAreaDetails(it.areacode, it.sigungucode)
        }.distinct().sorted().toMutableList()
        cities.add(0, "전체")
        _localList.value = cities
    }

    fun onClickIconHeart(contentId: String) {
        val current = _likeMap.value.toMutableMap()
        val isLiked = !(current[contentId] ?: false)
        current[contentId] = isLiked
        _likeMap.value = current

        viewModelScope.launch {
            if (isLiked) {
                userService.addLikeItem(tripApp.loginUserModel.userDocId, contentId)
                userService.addLikeCnt(contentId)
            } else {
                userService.removeLikeItem(tripApp.loginUserModel.userDocId, contentId)
                userService.removeLikeCnt(contentId)
                _interestingListAll.value = _interestingListAll.value.filter { it.contentID != contentId }
            }
        }
    }

    fun setFilteredCity(city: String) {
        _filteredCityName.value = city
    }

    fun toggleAttraction() {
        _isCheckAttraction.value = !_isCheckAttraction.value
    }

    fun toggleRestaurant() {
        _isCheckRestaurant.value = !_isCheckRestaurant.value
    }

    fun toggleAccommodation() {
        _isCheckAccommodation.value = !_isCheckAccommodation.value
    }

    fun onClickListItemToDetailScreen(contentId: String) {
        tripApp.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL}/$contentId")
    }

    fun onClickNavIconBack() {
        tripApp.navHostController.popBackStack()
    }

    fun setSheetOpen(open: Boolean) {
        _isSheetOpen.value = open
    }
}
