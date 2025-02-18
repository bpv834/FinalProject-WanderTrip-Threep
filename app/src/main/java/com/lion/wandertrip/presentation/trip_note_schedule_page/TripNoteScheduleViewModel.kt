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
import com.lion.wandertrip.util.TripNoteScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TripNoteScheduleViewModel @Inject constructor(
    @ApplicationContext val context: Context,
) : ViewModel() {

    var tripNoteScheduleList = mutableStateListOf<TripScheduleModel>()

    val tripApplication = context as TripApplication

    // 리사이클러뷰 임시 데이터 리스트 (내 일정)
    fun gettingTripNoteScheduleData() {
        tripNoteScheduleList = mutableStateListOf(
            TripScheduleModel(
                scheduleTitle = "제주 힐링여행",
                scheduleStartDate = Timestamp.now(),
                scheduleCity = "제주",
                scheduleEndDate = Timestamp.now(),
            ),
            TripScheduleModel(
                scheduleTitle = "서울 힐링여행",
                scheduleStartDate = Timestamp.now(),
                scheduleCity = "서울",
                scheduleEndDate = Timestamp.now(),
            )
        )
    }

    // 뒤로 가기 버튼
    fun navigationButtonClick(){
        tripApplication.navHostController.popBackStack()
    }

    // 일정 가져오기 버튼
    fun gettingSchedule(tripSchedule: TripScheduleModel){
        val scheduleTitle = tripSchedule.scheduleTitle
        Log.d("GettingSchedule", "Schedule Title: $scheduleTitle")
        tripApplication.navHostController.popBackStack()
        tripApplication.navHostController.popBackStack()
        tripApplication.navHostController.navigate("${TripNoteScreenName.TRIP_NOTE_WRITE.name}/${scheduleTitle}")
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

}