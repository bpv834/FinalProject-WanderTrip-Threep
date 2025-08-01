package com.lion.wandertrip.presentation.schedule_city_select

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctionsException
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.AreaCode
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.RotateMapScreenName
import com.lion.wandertrip.util.RouletteScreenName
import com.lion.wandertrip.util.ScheduleScreenName
import com.lion.wandertrip.util.Tools
import com.lion.wandertrip.util.Tools.Companion.getLatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.checkerframework.checker.units.qual.Area
import javax.inject.Inject

@HiltViewModel
class ScheduleCitySelectViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val tripScheduleService: TripScheduleService,
    private val userService: UserService,
) : ViewModel() {
    // 내부에서만 수정 가능한 MutableStateFlow
    private val _searchQuery = MutableStateFlow("")
    // 외부에는 읽기 전용 StateFlow로 노출
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterList = MutableStateFlow<List<String>>(emptyList())
    val filterList :StateFlow<List<String>> = _filterList

    // 🔄 쿼리 변경 함수
    fun updateQuery(newQuery: String) {
        _searchQuery.value = newQuery
       _filterList.value =  Tools.searchRegionNames(_searchQuery.value)
    }

    // ✅ 검색 수행 시 처리
    fun onClickToResult(query: String) {
        // TODO: 검색 로직 실행
    }

    val application = context as TripApplication

    val isLoading = mutableStateOf(false)

    // 일정 모델
    val tripScheduleModel = TripScheduleModel()

    val initCities = listOf<String>(
        "서울",
        "가평",
        "양평",
        "강릉",
        "속초",
        "경주",
        "부산",
        "여수",
        "인천",
        "전주",
        "제주",
        "춘천",
        "홍천",
        "태안",
        "통영",
        "거제",
        "포항",
        "안동"
    )

    val scheduleTitle = mutableStateOf<String>("")
    val scheduleStartDate = mutableStateOf<Timestamp>(Timestamp.now())
    val scheduleEndDate = mutableStateOf<Timestamp>(Timestamp.now())


    // 처음 데이터 설정
    fun settingFirstData(title: String, startDate: Timestamp, endDate: Timestamp) {
        scheduleTitle.value = title
        scheduleStartDate.value = startDate
        scheduleEndDate.value = endDate
    }

    // 특정 기간 동안의 날짜 목록 생성 함수
    fun generateDateList(startDate: Timestamp, endDate: Timestamp): List<Timestamp> {
        val dateList = mutableListOf<Timestamp>()
        var currentTimestamp = startDate

        while (currentTimestamp.seconds <= endDate.seconds) {
            dateList.add(currentTimestamp)
            currentTimestamp =
                Timestamp(currentTimestamp.seconds + 86400, 0) // 하루(24시간 = 86400초) 추가
        }

        return dateList
    }

    // 한반도 돌리기 화면으로 이동
    fun moveToRotateMapScreen(
        scheduleTitle: String,
        scheduleStartDate: Timestamp,
        scheduleEndDate: Timestamp
    ) {
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

    // 이전 화면 으로 돌아 가기
    fun backScreen() {
        application.navHostController.popBackStack()
    }

    // 일정 상세 화면 으로 이동
    fun moveToScheduleDetailRandomScreen(lat: String, lng: String) {
        Log.d(
            "moveDetail",
            "tripScheduleModel.tripScheduleDocId: ${tripScheduleModel.tripScheduleDocId}"
        )
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
    ) {

        isLoading.value = true
        val scheduleDateList = generateDateList(scheduleStartDate, scheduleEndDate)
        tripScheduleModel.userID = application.loginUserModel.userId
        tripScheduleModel.userNickName = application.loginUserModel.userNickName
        tripScheduleModel.scheduleCity = ""
        tripScheduleModel.scheduleTitle = scheduleTitle
        tripScheduleModel.scheduleStartDate = scheduleStartDate
        tripScheduleModel.scheduleEndDate = scheduleEndDate
        tripScheduleModel.scheduleDateList = scheduleDateList
        tripScheduleModel.scheduleInviteList += application.loginUserModel.userDocId
        tripScheduleModel.lat = 0.0
        tripScheduleModel.lng = 0.0
        tripScheduleModel.scheduleCity = areaName
        viewModelScope.launch {

            // 위도경도 구해서 넣는 메서드 실행
            withContext(Dispatchers.IO) {
                val getLatLng = getLatLng(areaName,application)
                tripScheduleModel.lat = getLatLng?.first ?: 0.0
                tripScheduleModel.lng = getLatLng?.second ?: 0.0
            }

            // tripScheduleModel 생성 및 Firestore에 추가
            val tripScheduleDocId = withContext(Dispatchers.IO) {
                tripScheduleService.addTripSchedule(tripScheduleModel)
            }
            tripScheduleModel.tripScheduleDocId = tripScheduleDocId

            /*           // 사용자의 서브컬렉션에 추가
                       withContext(Dispatchers.IO) {
                           userService.addTripScheduleToUserSubCollection(
                               application.loginUserModel.userDocId,
                               tripScheduleDocId
                           )
                       }*/

            // 모든 작업이 끝난 뒤 화면 전환
            moveToScheduleDetailRandomScreen(
                tripScheduleModel.lat.toString(),
                tripScheduleModel.lng.toString()
            )
        }
    }


}