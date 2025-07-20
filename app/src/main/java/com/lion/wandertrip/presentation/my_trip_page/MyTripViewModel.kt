package com.lion.wandertrip.presentation.my_trip_page

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.ScheduleScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    // ViewModel 내부
    private val _tripList = MutableStateFlow<List<TripScheduleModel>>(emptyList())
    val tripList: StateFlow<List<TripScheduleModel>> = _tripList

    private val _upComingTripList = MutableStateFlow<List<TripScheduleModel>>(emptyList())
    val upComingTripList: StateFlow<List<TripScheduleModel>> = _upComingTripList

    private val _pastTripList = MutableStateFlow<List<TripScheduleModel>>(emptyList())
    val pastTripList: StateFlow<List<TripScheduleModel>> = _pastTripList

    // 인덱스별 메뉴 상태를 관리할 맵
    val menuStateMap = mutableStateMapOf<Int, Boolean>()

    // 화면 열때 리스트 가져오기
    fun getTripList() {
        // Log.d("test100","getTripList")
        viewModelScope.launch {
            // 일정 모델 리스트
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.gettingMyTripSchedules(tripApplication.loginUserModel.userNickName)
            }
            val result = work1.await()
            _tripList.value = result
            getUpComingList()
            getPastList()
            addMap()
        }

    }

    // 리스트 길이로 맵을 초기화
    fun addMap() {
        // Log.d("test100","addMap")
        tripList.value.forEachIndexed { index, tripScheduleModel ->
            menuStateMap[index] = false
        }
    }

    // 필터를 사용해 다가올 리스트 가져오기
    fun getUpComingList() {
        // Log.d("test100","getUpComingList")
        // 필터링하고 정렬된 리스트를 upComingTripList에 추가

        _upComingTripList.value =
            _tripList.value
                .filter { it.scheduleEndDate >= currentDate } // 현재 날짜 이후 여행 필터링
                .sortedBy { it.scheduleStartDate.toDate() } // scheduleStartDate가 가까운 순으로 정렬

    }

    // 지난 여행 가져오기
    fun getPastList() {
        // Log.d("test100","getPastList")
        _pastTripList.value = _tripList.value
            .filter { it.scheduleEndDate < currentDate }  // 현재 날짜 이전 여행 필터링
            .sortedByDescending { it.scheduleStartDate.toDate() } // scheduleEndDate가 가까운 순으로 정렬

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
            val work0 = async(Dispatchers.IO) {
                userService.deleteTripScheduleItem(
                    tripApplication.loginUserModel.userDocId,
                    trip.tripScheduleDocId
                )
            }
            work0.join()

            // 일정 참여자가 1이하면 trip 모델 자체를 삭제
            if (trip.scheduleInviteList.size < 2) {
                tripScheduleService.deleteTripScheduleByDocId(trip.tripScheduleDocId)
            } else {
                // 참여자가 2 이상이면 초대리스트에서 해당 유저만 제거
                tripScheduleService.removeScheduleInviteList(
                    trip.tripScheduleDocId,
                    tripApplication.loginUserModel.userDocId
                )
            }
            getTripList()
        }

    }

    // 내 여행 상세로 화면 전환 메서드
    fun onClickScheduleItemGoScheduleDetail(tripSchedule: TripScheduleModel) {
        tripApplication.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_RANDOM_SCREEN.name}?" +
                    "tripScheduleDocId=${tripSchedule.tripScheduleDocId}&lat=${tripSchedule.lat}&lng=${tripSchedule.lng}",
        ) {
            // 예를 들어 버튼 연타할 때 같은 화면이 여러 번 쌓이는 것 방지
            launchSingleTop = true // 새 인스턴스를 만들지 않고, 기존 인스턴스를 재사용함
        }
    }
}