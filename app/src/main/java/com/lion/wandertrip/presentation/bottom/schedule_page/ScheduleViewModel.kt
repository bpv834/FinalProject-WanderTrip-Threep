package com.lion.wandertrip.presentation.bottom.schedule_page

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.ScheduleScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripScheduleService: TripScheduleService,
    val userService: UserService,
) : ViewModel() {

    val application = context as TripApplication

    // 데이터 로딩 상태
    // ViewModel 안에서 선언
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 일정 추가 버튼 클릭 이벤트
    fun addIconButtonEvent() {
        application.navHostController.navigate("${ScheduleScreenName.SCHEDULE_ADD_SCREEN.name}/")
    }

    // ✅ Timestamp -> "YYYY.MM.DD" 형식 변환 함수
    fun formatTimestampToDateString(timestamp: Timestamp): String {
        val localDate = Instant.ofEpochMilli(timestamp.seconds * 1000)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd") // ✅ 년-월-일 포맷 적용
        return localDate.format(formatter)
    }

    // 일정 제거
    // 일정 가입 인원이 0명인 경우 일정 컬렉션에서 제거
    fun deleteScheduleIfZeroParty(trip: TripScheduleModel) {
        if (trip.scheduleInviteList.size == 1) {
            viewModelScope.launch {
                val work1 = async(Dispatchers.IO) {
                    tripScheduleService.deleteTripScheduleByDocId(trip.tripScheduleDocId)
                }
                work1.join()
            }
        }
    }

    // 내 일정 삭제
    fun removeUserSchedule(tripScheduleDocId: String) {
        Log.d("ScheduleViewModel", "deleteSchedule: $tripScheduleDocId")
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.removeUserScheduleList(
                    application.loginUserModel.userDocId,
                    tripScheduleDocId
                )
            }.join()

            val work2 = async(Dispatchers.IO) {
                tripScheduleService.removeScheduleInviteList(
                    tripScheduleDocId,
                    application.loginUserModel.userDocId
                )
            }.join()
            loadMyScheduleOnce()
        }

    }

    // 초대 받은 일정 삭제
    fun removeInvitedSchedule(tripScheduleDocId: String) {
        Log.d("ScheduleViewModel", "deleteSchedule: $tripScheduleDocId")
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.removeInvitedScheduleList(
                    application.loginUserModel.userDocId,
                    tripScheduleDocId
                )
            }.await()

            val work2 = async(Dispatchers.IO) {
                tripScheduleService.removeScheduleInviteList(
                    tripScheduleDocId,
                    application.loginUserModel.userDocId
                )
            }.await()
        }
    }


    // 일정 상세 화면 으로 이동
    fun moveToScheduleDetailScreen(scheduleModel: TripScheduleModel) {
        val lat = scheduleModel.lat
        val lng = scheduleModel.lng

        // 일정 상세 화면 이동
        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_RANDOM_SCREEN.name}?" +
                    "tripScheduleDocId=${scheduleModel.tripScheduleDocId}&lat=${lat}&lng=${lng}",
        ) {
            popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) { inclusive = false }
            launchSingleTop = true
        }
    }




    // flow 유저 스케줄 상태 변수
    private val _userSchedules = MutableStateFlow<List<TripScheduleModel>>(emptyList())
    val userSchedules: StateFlow<List<TripScheduleModel>> = _userSchedules

    // flow 유저 스케줄 collect 메서드
    fun loadMyScheduleOnce() {
        viewModelScope.launch {
            val result = tripScheduleService.gettingMyTripSchedules(application.loginUserModel.userNickName)
            _userSchedules.value = result
                .filter { it.scheduleStartDate > Timestamp.now() }.sortedBy { it.scheduleStartDate } // 다가오는 일정
        }
    }

}