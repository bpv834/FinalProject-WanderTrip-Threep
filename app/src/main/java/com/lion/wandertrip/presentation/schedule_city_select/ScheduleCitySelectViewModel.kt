package com.lion.wandertrip.presentation.schedule_city_select

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
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.RotateMapScreenName
import com.lion.wandertrip.util.RouletteScreenName
import com.lion.wandertrip.util.ScheduleScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.Area
import javax.inject.Inject

@HiltViewModel
class ScheduleCitySelectViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val tripScheduleService: TripScheduleService,
    private val userService: UserService,
) : ViewModel() {

    val application = context as TripApplication

    val isLoading = mutableStateOf(false)
    
    // 일정 모델
    val tripScheduleModel = TripScheduleModel()

    // 모든 도시 리스트
    val allCities = mutableStateListOf<AreaCode>()
    // 검색 결과 리스트
    val filteredCities = mutableStateListOf<AreaCode>().apply { addAll(allCities) }

    val scheduleTitle = mutableStateOf<String>("")
    val scheduleStartDate = mutableStateOf<Timestamp>(Timestamp.now())
    val scheduleEndDate = mutableStateOf<Timestamp>(Timestamp.now())

    init {
        // 모든 도시 추가
        addCity()
        // 초기 화면에 모든 도시 표시
        filteredCities.addAll(allCities)
    }

    // 모든 도시 추가
    fun addCity() {
        allCities.clear() // 혹시 모를 중복 방지
        allCities.addAll(AreaCode.entries.map { it }) // enum 모든 값 추가
    }

    // 처음 데이터 설정
    fun settingFirstData(title: String, startDate: Timestamp, endDate: Timestamp) {
        scheduleTitle.value = title
        scheduleStartDate.value = startDate
        scheduleEndDate.value = endDate
    }

    // ✅ 검색어에 맞는 도시 필터링
    fun updateFilteredCities(query: String) {
        if (query.isEmpty()) {
            filteredCities.clear()
            filteredCities.addAll(allCities) // 검색어가 없으면 전체 도시 표시
        } else {
            filteredCities.clear()
            filteredCities.addAll(allCities.filter { it.areaName.contains(query, ignoreCase = true) })
        }
    }

    // 특정 기간 동안의 날짜 목록 생성 함수
    fun generateDateList(startDate: Timestamp, endDate: Timestamp): List<Timestamp> {
        val dateList = mutableListOf<Timestamp>()
        var currentTimestamp = startDate

        while (currentTimestamp.seconds <= endDate.seconds) {
            dateList.add(currentTimestamp)
            currentTimestamp = Timestamp(currentTimestamp.seconds + 86400, 0) // 하루(24시간 = 86400초) 추가
        }

        return dateList
    }

    // 도시 룰렛 화면 으로 이동
    fun moveToRouletteCityScreen(scheduleTitle: String, scheduleStartDate: Timestamp, scheduleEndDate: Timestamp) {
        // application.navHostController.navigate(RouletteScreenName.ROULETTE_CITY_SCREEN.name)

        val formattedTitle = scheduleTitle
        val startTimestamp = scheduleStartDate.seconds // 🔹 Timestamp -> Long 변환
        val endTimestamp = scheduleEndDate.seconds // 🔹 Timestamp -> Long 변환

        application.navHostController.navigate(
            "${RouletteScreenName.ROULETTE_CITY_SCREEN.name}?" +
                    "scheduleTitle=$formattedTitle" +
                    "&scheduleStartDate=$startTimestamp" +
                    "&scheduleEndDate=$endTimestamp"
        )
    }

    // 한반도 돌리기 화면으로 이동
    fun moveToRotateMapScreen(scheduleTitle: String, scheduleStartDate: Timestamp, scheduleEndDate: Timestamp) {
        // application.navHostController.navigate(RouletteScreenName.ROULETTE_CITY_SCREEN.name)

        val formattedTitle = scheduleTitle
        val startTimestamp = scheduleStartDate.seconds // 🔹 Timestamp -> Long 변환
        val endTimestamp = scheduleEndDate.seconds // 🔹 Timestamp -> Long 변환

        application.navHostController.navigate(
            "${RotateMapScreenName.ROTATE_MAP_SCREEN.name}?" +
                    "scheduleTitle=$formattedTitle" +
                    "&scheduleStartDate=$startTimestamp" +
                    "&scheduleEndDate=$endTimestamp"
        )
    }

    // 일정 상세 화면 으로 이동
    fun moveToScheduleDetailScreen(areaName: String, areaCode: Int) {
        Log.d("ScheduleCitySelectViewModel", "도시 이름: $areaName, 도시 코드: $areaCode")
        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
                    "tripScheduleDocId=${tripScheduleModel.tripScheduleDocId}&areaName=${areaName}&areaCode=${areaCode}"
        ) {
            popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) { inclusive = false }
            launchSingleTop = true
        }
    }

    // 이전 화면 으로 돌아 가기
    fun backScreen() {
        application.navHostController.popBackStack()
    }

    // 일정 추가
    fun addTripSchedule(
        scheduleTitle: String,
        scheduleStartDate: Timestamp,
        scheduleEndDate: Timestamp,
        areaName: String,
        areaCode: Int
    ) {
        isLoading.value = true
        val scheduleDateList = generateDateList(scheduleStartDate, scheduleEndDate)
        tripScheduleModel.userID = application.loginUserModel.userId
        tripScheduleModel.userNickName = application.loginUserModel.userNickName
        tripScheduleModel.scheduleCity = areaName
        tripScheduleModel.scheduleTitle = scheduleTitle
        tripScheduleModel.scheduleStartDate = scheduleStartDate
        tripScheduleModel.scheduleEndDate = scheduleEndDate
        tripScheduleModel.scheduleDateList = scheduleDateList
        tripScheduleModel.scheduleInviteList += application.loginUserModel.userDocId

        Log.d("ScheduleCitySelectViewModel", "userDocId: ${application.loginUserModel.userDocId}")


        viewModelScope.launch {
            val work = async(Dispatchers.IO) {
                tripScheduleService.addTripSchedule(tripScheduleModel)
            }.await()
            tripScheduleModel.tripScheduleDocId = work
            Log.d("ScheduleCitySelectViewModel", "tripScheduleModel.tripScheduleDocId: ${tripScheduleModel.tripScheduleDocId}")
            Log.d("ScheduleCitySelectViewModel", "tripScheduleModel.userID: ${application.loginUserModel.userId}")

            val work2 = async(Dispatchers.IO) {
                userService.addTripScheduleToUserSubCollection(
                    application.loginUserModel.userDocId,
                    tripScheduleModel.tripScheduleDocId
                )
            }.await()

            delay(2000)

            // 일정 상세 화면 으로 이동
            moveToScheduleDetailScreen(areaName, areaCode)
        }
    }
}