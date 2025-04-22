package com.lion.wandertrip.presentation.bottom.schedule_page

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.AreaCode
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
    val tripScheduleService : TripScheduleService,
    val userService: UserService,
) : ViewModel() {

    val application = context as TripApplication

    val myScheduleList = mutableStateListOf<TripScheduleModel>()

    // 데이터 로딩 상태
    // ViewModel 안에서 선언
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 유저 일정 docId로 일정 항목 가져 오기
    fun fetchUserScheduleList() {
        myScheduleList.clear()
        Log.d("test100","fetchUserScheduleList")
        viewModelScope.launch {
            _isLoading.value=true

            // 유저의 일정 서브컬렉션에서 일정 문서 아이디 리스트 가져오기
            val work0 = async(Dispatchers.IO){
                userService.gettingTripScheduleItemList(application.loginUserModel.userDocId)
            }
            // 유저의 일정 문서 아이디 리스트
            val userScheduleDocIdList = work0.await()
            // 문서 아이디를 통해서 tripSchedule 모델 가져오기
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.fetchScheduleList(userScheduleDocIdList)
            }
            myScheduleList.addAll(
                // 다가오는 일정만 내 일정에 표시
                // 지난 일정은 my -> 내일정에서 보여주기
                work1.await().filter { it.scheduleEndDate> Timestamp.now() }.sortedBy { it.scheduleEndDate }
            )
            _isLoading.value=false

        }
    }

    // 일정 추가 버튼 클릭 이벤트
    fun addIconButtonEvent() {
        application.navHostController.navigate( "${ScheduleScreenName.SCHEDULE_ADD_SCREEN.name}/")
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
    fun deleteScheduleIfZeroParty(trip : TripScheduleModel){
        if(trip.scheduleInviteList.size==1){
            viewModelScope.launch {
                val work1 = async(Dispatchers.IO){
                    tripScheduleService.deleteTripScheduleByDocId(trip.tripScheduleDocId)
                }
                work1.join()
            }
        }
    }

    // 내 일정 삭제
    fun removeUserSchedule(tripScheduleDocId: String) {
        myScheduleList.clear()

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

            fetchUserScheduleList()
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



    // 해당 일정 상세 화면으로 이동
    fun moveToScheduleDetailScreen(scheduleModel: TripScheduleModel) {
        // scheduleCity와 일치하는 AreaCode 찾기 (없으면 0 반환)
        val areaCodeValue = AreaCode.entries.firstOrNull { it.areaName == scheduleModel.scheduleCity }?.areaCode ?: 0
        Log.d("ScheduleViewModel", "areaCodeValue: $areaCodeValue")

        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
                    "tripScheduleDocId=${scheduleModel.tripScheduleDocId}&areaName=${scheduleModel.scheduleCity}&areaCode=$areaCodeValue"
        )
    }

}