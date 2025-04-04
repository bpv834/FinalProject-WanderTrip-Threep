package com.lion.wandertrip.presentation.my_trip_page

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.presentation.my_trip_page.used_dummy_data.ComeAndPastScheduleDummyData
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.AreaCode
import com.lion.wandertrip.util.ScheduleScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyTripViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val userService: UserService,
    val tripScheduleService: TripScheduleService,
) : ViewModel() {
    // 현재 날짜 가져오기
    val currentDate = Timestamp.now()

    // 여행 데이터 전체
    val tripList = mutableStateListOf<TripScheduleModel>()

    // 다가올 여행
    val upComingTripList = mutableStateListOf<TripScheduleModel>()

    // 지난 여행
    val pastTripList = mutableStateListOf<TripScheduleModel>()

    // 인덱스별 메뉴 상태를 관리할 맵
    val menuStateMap = mutableStateMapOf<Int, Boolean>()

    // 화면 열때 리스트 가져오기
    fun getTripList() {
        tripList.clear()
        // Log.d("test100","getTripList")
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                userService.gettingTripScheduleItemList(tripApplication.loginUserModel.userDocId)
            }
            // 유저 목록의 일정 서브컬렉션 문서 아이디 리스트
            val tripDocIdList = work1.await()

            // 일정 모델 리스트
            val work2 = async(Dispatchers.IO) {
                tripScheduleService.fetchScheduleList(tripDocIdList)
            }
            val result = work2.await()
            tripList.addAll(result)
            getUpComingList()
            getPastList()
            addMap()
        }

    }

    // 리스트 길이로 맵을 초기화
    fun addMap() {
        // Log.d("test100","addMap")
        tripList.forEachIndexed { index, tripScheduleModel ->
            menuStateMap[index] = false
        }
    }

    // 필터를 사용해 다가올 리스트 가져오기
    fun getUpComingList() {
        // Log.d("test100","getUpComingList")
        // 필터링하고 정렬된 리스트를 upComingTripList에 추가
        upComingTripList.clear()
        upComingTripList.addAll(
            tripList
                .filter { it.scheduleEndDate >= currentDate } // 현재 날짜 이후 여행 필터링
                .sortedBy { it.scheduleStartDate.toDate() } // scheduleStartDate가 가까운 순으로 정렬
        )
    }

    // 지난 여행 가져오기
    fun getPastList() {
        // Log.d("test100","getPastList")
        pastTripList.clear()
        pastTripList.addAll(
            tripList
                .filter { it.scheduleEndDate < currentDate }  // 현재 날짜 이전 여행 필터링
                .sortedByDescending { it.scheduleStartDate.toDate() } // scheduleEndDate가 가까운 순으로 정렬
        )
    }

    // context 변수
    val tripApplication = context as TripApplication

    // 이전에 눌렸던 메뉴 인덱스 상태
    var truedIdx = mutableStateOf(-1)

    // 메뉴 상태 관리 변수
    var isMenuOpened = mutableStateOf(false)

    // 메뉴가 눌릴 때 리스너
    fun onClickIconMenu(clickPos: Int) {
        // 한번이라도 메뉴가 클릭된적이 없다면
        if (!isMenuOpened.value) {
            menuStateMap[clickPos] = true
            isMenuOpened.value = true
            truedIdx.value = clickPos

        } else {
            // 한번이상 메뉴가 클릭됐다면
            menuStateMap[truedIdx.value] = false
            menuStateMap[clickPos] = true
            truedIdx.value = clickPos
        }

    }

    // 뒤로가기
    fun onClickNavIconBack() {
        tripApplication.navHostController.popBackStack()
    }
    // 여행 날짜 변경 메서드

    // 여행 삭제 메서드
    fun onClickIconDeleteTrip(trip: TripScheduleModel) {
        viewModelScope.launch {
            // Log.d("test100","onClickIconDeleteTrip")

            // 유저 일정 서브컬렉션에서 제거
            val work0 = async(Dispatchers.IO){
                userService.deleteTripScheduleItem(tripApplication.loginUserModel.userDocId,trip.tripScheduleDocId)
            }
            work0.join()

            // 일정 참여자가 1이하면 trip 모델 자체를 삭제
            if (trip.scheduleInviteList.size < 2) {
                tripScheduleService.deleteTripScheduleByDocId(trip.tripScheduleDocId)
            } else {
                // 참여자가 2 이상이면 초대리스트에서 해당 유저만 제거
                tripScheduleService.removeScheduleInviteList(trip.tripScheduleDocId,tripApplication.loginUserModel.userDocId)
            }
            tripList.clear()
            getTripList()
        }

    }

    // 내 여행 상세로 화면 전환 메서드
    fun onClickScheduleItemGoScheduleDetail(tripScheduleDocId: String, areaName: String) {

        // scheduleCity와 일치하는 AreaCode 찾기 (없으면 0 반환)
        val areaCodeValue = AreaCode.entries.firstOrNull { it.areaName == areaName }?.areaCode ?: 0
        Log.d("ScheduleViewModel", "areaCodeValue: $areaCodeValue")


        tripApplication.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
                    "tripScheduleDocId=${tripScheduleDocId}&areaName=${areaName}&areaCode=$areaCodeValue"
        )
    }
}