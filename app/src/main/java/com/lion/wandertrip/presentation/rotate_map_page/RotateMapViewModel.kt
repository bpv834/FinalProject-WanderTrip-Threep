package com.lion.wandertrip.presentation.rotate_map_page

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripLocationBasedItemService
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.ContentTypeId
import com.lion.wandertrip.util.ScheduleScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class RotateMapViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripScheduleService: TripScheduleService,
    val tripLocationBasedItemService: TripLocationBasedItemService,
    val userService : UserService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // context
    val tripApplication = context as TripApplication

    // set 제한 lat 변수
    var initLat by mutableStateOf("")
        private set

    // set 제한 lng 변수
    var initLng by mutableStateOf("")
        private set

    // 위도경도 설정 메서드
    fun setLatLng(lat: String, lng: String) {
        initLat = lat
        initLng = lng
    }

    // 일정 모델
    val tripScheduleModel = mutableStateOf(TripScheduleModel())

    // 회전상태
    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning

    // 회전 상태 변경
    fun startSpinning() {
        _isSpinning.value = true
    }

    fun stopSpinning() {
        _isSpinning.value = false
    }

    // 회전각 상태
    private val _targetRotation = MutableStateFlow(0f)
    val targetRotation: StateFlow<Float> = _targetRotation

    // ratation set 메서드
    fun setTargetRotation(target: Float) {
        _targetRotation.value = target
    }

    // 관광지 타입 변수
    private val ATTRACTION_CONTENT_TYPE_ID =
        ContentTypeId.TOURIST_ATTRACTION.contentTypeCode.toString()

    // 해당지역 관광지 있을때 보여주는 로그
    private val _showAttractionDialog = MutableStateFlow(false)
    val showAttractionDialog: StateFlow<Boolean> = _showAttractionDialog

    // 관광지 있을때 다이얼로그 보이는 메서드
    fun showAttractionDialog() {
        _showAttractionDialog.value = true
    }

    // 관광지 있을 때 다이얼로그 숨기는 메서드
    fun hideAttractionDialog() {
        _showAttractionDialog.value = false
    }

    // 해당지역 관광지 없을때 보여주는 로그
    private val _showNoAttractionDialog = MutableStateFlow(false)
    val showNoAttractionDialog: StateFlow<Boolean> = _showNoAttractionDialog

    // 관광지 없을 때 다이얼로그 보이는 메서드
    fun showNoAttractionDialog() {
        _showNoAttractionDialog.value = true
    }

    // 관광지 있을 때 다이얼로그 숨기는 메서드
    fun hideNoAttractionDialog() {
        _showNoAttractionDialog.value = false
    }

    // 관광지 존재 여부만 판단
    suspend fun isFindAttraction(lat: String, lng: String): Boolean {
        val (publicItemList, totalCount) = tripLocationBasedItemService.gettingTripLocationBasedItemList(
            lat = lat,
            lng = lng,
            contentTypeId = ATTRACTION_CONTENT_TYPE_ID,
            page = 1,
            radius = "10",
            numOfRows = 4
        )
        return totalCount != 0
    }

    fun onRotationFinished(lat: String, lon: String) {
        _isSpinning.value = false

        viewModelScope.launch {
            val hasAttractions = isFindAttraction(lat, lon)
            delay(2100)

            if (hasAttractions) {
                showAttractionDialog()
            } else {
                showNoAttractionDialog()
            }
        }
    }


    // 회전 보정 함수
    fun rotatePointBack(tap: Offset, size: IntSize, rotationDegrees: Float): Offset {
        val center = Offset(size.width / 2f, size.height / 2f)

        val dx = tap.x - center.x
        val dy = tap.y - center.y

        val radians = Math.toRadians(-rotationDegrees.toDouble())

        val rotatedX = (dx * cos(radians) - dy * sin(radians)).toFloat()
        val rotatedY = (dx * sin(radians) + dy * cos(radians)).toFloat()

        val corrected = Offset(rotatedX + center.x, rotatedY + center.y)

        return Offset(
            corrected.x / size.width,
            corrected.y / size.height
        )
    }

    // 위도/경도 변환 함수
    fun toLatLng(relativeOffset: Offset): Pair<Double, Double> {
        val latTop = 38.676696
        val latBottom = 33.226696
        val lonLeft = 125.89075
        val lonRight = 129.49075

        val latitude = latTop + (latBottom - latTop) * relativeOffset.y
        val longitude = lonLeft + (lonRight - lonLeft) * relativeOffset.x

        return latitude to longitude
    }

    // 일정 상세 화면 으로 이동
    fun moveToScheduleDetailRandomScreen(lat: String, lng: String) {

        // 일정 상세 화면 이동
        tripApplication.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_RANDOM_SCREEN.name}?" +
                    "tripScheduleDocId=${tripScheduleModel.value.tripScheduleDocId}&lat=${lat}&lng=${lng}",
        ) {
            popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) { inclusive = false }
            launchSingleTop = true
        }
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

    // 일정 추가
    fun addTripSchedule(
        scheduleTitle: String,
        scheduleStartDate: Timestamp,
        scheduleEndDate: Timestamp,
        areaName: String,
        lat: String,
        lng: String
    ) {
        hideAttractionDialog()
        hideNoAttractionDialog()

        val scheduleDateList = generateDateList(scheduleStartDate, scheduleEndDate)

        val tripScheduleItem = TripScheduleModel().let {
            it.userID = tripApplication.loginUserModel.userId
            it.userNickName = tripApplication.loginUserModel.userNickName
            it.scheduleCity = areaName
            it.scheduleTitle = scheduleTitle
            it.scheduleStartDate = scheduleStartDate
            it.scheduleEndDate = scheduleEndDate
            it.scheduleDateList = scheduleDateList
            it.scheduleInviteList += tripApplication.loginUserModel.userDocId
            it.lat = lat
            it.lng = lng
            it
        }
        tripScheduleModel.value = tripScheduleItem

        viewModelScope.launch {
            val work = async(Dispatchers.IO) {
                tripScheduleService.addTripSchedule(tripScheduleModel.value)
            }.await()
            tripScheduleModel.value.tripScheduleDocId = work

            val work2 = async(Dispatchers.IO) {
                userService.addTripScheduleToUserSubCollection(
                    tripApplication.loginUserModel.userDocId,
                    tripScheduleModel.value.tripScheduleDocId
                )
            }.await()

            // 일정 상세 화면 으로 이동
            moveToScheduleDetailRandomScreen(lat, lng)
        }
    }
}