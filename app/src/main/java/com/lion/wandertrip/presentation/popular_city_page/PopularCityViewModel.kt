package com.lion.wandertrip.presentation.popular_city_page

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.service.ContentsService
import com.lion.wandertrip.service.TripLocationBasedItemService
import com.lion.wandertrip.service.TripNoteService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PopularCityViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val tripNoteService: TripNoteService,
    val contentsService: ContentsService,
    val tripLocationBasedItemService: TripLocationBasedItemService
    ) : ViewModel() {
    val tripApplication = context as TripApplication

    private val _tripNoteList = MutableStateFlow<List<TripNoteModel>>(emptyList())
    val tripNoteList: StateFlow<List<TripNoteModel>> get() = _tripNoteList

    private val _spotItemList = MutableStateFlow<List<TripLocationBasedItem>>(emptyList())
    val tripList: StateFlow<List<TripLocationBasedItem>> get() = _spotItemList



    // 여행기 가져오기
    // 도시 이름으로 가져오는데 문제가 생김
    fun getTripNoteList() {
        viewModelScope.launch {
            val result = tripNoteService.gettingTripNoteList()
            _tripNoteList.value = result
        }
    }

    // 해당지역 관광지 가져오기
    fun getSpotList(lat : String, lng:String, contentTypeId:String, page : Int =1 , radius:String) {
        viewModelScope.launch {
            val result = tripLocationBasedItemService.gettingTripLocationBasedItemList(lat,lng,contentTypeId,page,radius)
            _spotItemList.value = result
        }
    }

}