package com.lion.wandertrip.presentation.schedule_city_select

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
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

    // 지역 이름에 따른 기본 위치 좌표 반환 함수
    fun getDefaultLocation(areaName: String): LatLng {
        return when (areaName) {
            "서울" -> LatLng(37.5665, 126.9780)
            "인천" -> LatLng(37.4563, 126.7052)
            "대전" -> LatLng(36.3504, 127.3845)
            "대구" -> LatLng(35.8722, 128.6025)
            "광주" -> LatLng(35.1595, 126.8526)
            "부산" -> LatLng(35.1796, 129.0756)
            "울산" -> LatLng(35.5384, 129.3114)
            "세종시" -> LatLng(36.4800, 127.2890)
            "경기도" -> LatLng(37.4138, 127.5183)
            "강원도" -> LatLng(37.7519, 128.8969)
            "충청북도" -> LatLng(36.6357, 127.4910)
            "충청남도" -> LatLng(36.5184, 126.8000)
            "경상북도" -> LatLng(36.4919, 128.8889)
            "경상남도" -> LatLng(35.2383, 128.6920)
            "전라북도" -> LatLng(35.7175, 127.1441)
            "전라남도" -> LatLng(34.8161, 126.4630)
            "제주도" -> LatLng(33.4996, 126.5312)
            else -> LatLng(37.5665, 126.9780) // 기본값은 서울
        }
    }
    // 일정 상세 화면 으로 이동
    fun moveToScheduleDetailRandomScreen(lat: String, lng: String) {

        // 일정 상세 화면 이동
        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_RANDOM_SCREEN.name}?" +
                    "tripScheduleDocId=${tripScheduleModel.tripScheduleDocId}&lat=${lat}&lng=${lng}",
        ) {
            popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) { inclusive = false }
            launchSingleTop = true
        }
    }

    // 일정 추가
    fun addTripSchedule(
        scheduleTitle: String,
        scheduleStartDate: Timestamp,
        scheduleEndDate: Timestamp,
        areaName: String,
        areaCode: Int
    ) {
        val latLng = getDefaultLocation(areaName)
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
        tripScheduleModel.lat = latLng.latitude.toString()
        tripScheduleModel.lng = latLng.longitude.toString()

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
            moveToScheduleDetailRandomScreen(latLng.latitude.toString(), latLng.longitude.toString())

        }
    }
}