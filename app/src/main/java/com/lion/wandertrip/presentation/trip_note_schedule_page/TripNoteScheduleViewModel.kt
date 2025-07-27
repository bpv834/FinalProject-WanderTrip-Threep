package com.lion.wandertrip.presentation.trip_note_schedule_page

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.TripNoteScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TripNoteScheduleViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripNoteService: TripNoteService,
    val tripScheduleService: TripScheduleService
) : ViewModel() {
    val tripApplication = context as TripApplication
    val userDocId = tripApplication.loginUserModel.userDocId

    private val _scheduleList = MutableStateFlow<List<TripScheduleModel>>(emptyList())
    val scheduleList: StateFlow<List<TripScheduleModel>> = _scheduleList

    // 리사이클러뷰 데이터 리스트 (로그인한 사용자의 일정)
    fun gettingTripNoteScheduleData() {
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.gettingMyTripSchedules(tripApplication.loginUserModel.userNickName)
            }
            _scheduleList.value = work1.await().filter { it.scheduleStartDate <= Timestamp.now() } // 갔다온것만 여행기 쓰도록
                .sortedBy { it.scheduleStartDate }
        }
    }

    // 뒤로 가기 버튼
    fun navigationButtonClick() {
        tripApplication.navHostController.popBackStack()
    }

    // 일정 가져오기 버튼
    // 아이템 클릭 리스너
    fun gettingSchedule(tripSchedule: TripScheduleModel) {
        val scheduleDocId = tripSchedule.tripScheduleDocId

        tripApplication.navHostController.navigate(
            "${TripNoteScreenName.TRIP_NOTE_WRITE.name}?scheduleDocId=$scheduleDocId"
        ) {
            launchSingleTop = true
            popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) {
                inclusive = false
            }
        }
    }


    // ✅ Timestamp -> "YYYY.MM.DD" 형식 변환 함수
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTimestampToDateString(timestamp: Timestamp): String {
        val localDate = Instant.ofEpochMilli(timestamp.seconds * 1000)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd") // ✅ 년-월-일 포맷 적용
        return localDate.format(formatter)
    }

    init {
        gettingTripNoteScheduleData()
    }

}