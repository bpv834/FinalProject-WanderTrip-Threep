package com.lion.wandertrip.presentation.schedule_detail_page

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.util.ScheduleScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailRandomViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripScheduleService: TripScheduleService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val application = context as TripApplication

    // 일정 문서 ID
    val tripScheduleDocId: String = savedStateHandle["tripScheduleDocId"] ?: ""

    val lat = savedStateHandle["lat"]?:""
    val lng = savedStateHandle["lng"]?:""

    var selectedLocation = mutableStateOf<LatLng?>(null)
    var selectedDate = mutableStateOf<Timestamp?>(null)

    // val tripSchedule = TripScheduleModel()
    // 일정 데이터
    val tripSchedule = mutableStateOf(TripScheduleModel())

    // 일정 목록들 flow 변수
    private val _tripScheduleItems = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val tripScheduleItems: StateFlow<List<ScheduleItem>> = _tripScheduleItems

    // 일정 목록 flow 메서드
    fun startObservingTripItems() {
        viewModelScope.launch {
            // 서비스에서 흐름을 받아오고 collect로 stateFlow로 저장한다.
            // flow는 상태를 보관할수 없다.
            // stateFlow는 상태를 보관한다.
            tripScheduleService.getTripScheduleItemModelsFlow(tripScheduleDocId)
                .collect { items ->
                    _tripScheduleItems.value = items
                }
        }
    }

    // 로딩상태변수
    private val _isLoading = MutableStateFlow(false)
    val isLoading : StateFlow<Boolean> = _isLoading

    // 이전 화면 으로 이동 (메인 일정 화면)
    fun backScreen() {
        application.navHostController.popBackStack()
    }

    init {
        // 스케줄 가져오는 메서드 suspend 타입
        getTripSchedule()

        // 일정 목록 flow 메서드
        startObservingTripItems()
    }

    // 일정 상세 정보 가져오기
    fun getTripSchedule() {
        viewModelScope.launch {
            // isLoading.value = true // ✅ 로딩 시작

            val work1 = async(Dispatchers.IO) {
                tripScheduleService.getTripSchedule(tripScheduleDocId)
            }.await()

            if (work1 != null) {
                tripSchedule.value = work1
            } else {
                Log.d("ScheduleViewModel", "해당 문서가 없습니다.")
            }
            _isLoading.value = false
        }
    }

    // 함께 하는 친구 목록으로 이동
    fun moveToScheduleDetailFriendsScreen() {
        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_FRIENDS_SCREEN.name}?" +
                    "scheduleDocId=${tripScheduleDocId}&userNickName=${tripSchedule.value.userNickName}"
        )
    }

    // Timestamp를 "yyyy년 MM월 dd일" 형식의 문자열로 변환하는 함수
    fun formatTimestampToDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return sdf.format(Date(timestamp.seconds * 1000))
    }

    // 일정 항목 삭제 후 itemIndex 재조정
    fun removeTripScheduleItem(scheduleDocId: String, itemDocId: String, itemDate: Timestamp) {
        Log.d("removeTripScheduleItem", "scheduleDocId: $scheduleDocId, itemDocId: $itemDocId")
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.removeTripScheduleItem(scheduleDocId, itemDocId, itemDate)
            }.join()
        }
    }

    // 일정 항목 후기 화면으로 이동
    fun moveToScheduleItemReviewScreen(
        tripScheduleDocId: String,
        scheduleItemDocId: String,
        scheduleItemTitle: String,
    ) {
        application.navHostController.navigate(
            ScheduleScreenName.SCHEDULE_ITEM_REVIEW_SCREEN.name +
                    "/$tripScheduleDocId/$scheduleItemDocId/$scheduleItemTitle"
        )
    }

    // 완료 버튼 클릭 시, 임시 리스트 기준으로 각 항목의 itemIndex를 1부터 업데이트
    fun updateItemsOrderForDate(tempList : SnapshotStateList<ScheduleItem>, date: Timestamp) {
     // 완료 버튼 클릭 시, 임시 리스트 기준으로 각 항목의 itemIndex를 1부터 업데이트
        tempList.forEachIndexed { index, item ->
            item.itemIndex = index + 1
        }
        // viewModel의 전체 리스트에서 해당 날짜 그룹만 새 순서로 반영
        // 2. 기존 목록 중 해당 날짜만 교체
        _tripScheduleItems.value = _tripScheduleItems.value
            .map { item ->
                if (item.itemDate.seconds == date.seconds) {
                    tempList.find { it.itemDocId == item.itemDocId } ?: item
                } else {
                    item
                }
            }
            .sortedWith(compareBy({ it.itemDate.seconds }, { it.itemIndex }))

        // 변경한 위치를 데이터베이스에 업데이트
        updateItemsPosition(tempList)
    }

    // 위치 조정한 일정 항목 업데이트
    fun updateItemsPosition(updatedItems: List<ScheduleItem>) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.updateItemsPosition(tripScheduleDocId, updatedItems)
            }.await()
        }
    }

    // 일정 항목 선택 화면 으로 이동
    fun moveToScheduleRandomSelectItemScreen(itemCode: Int, scheduleDate: Timestamp) {
        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_RANDOM_SELECT_ITEM_SCREEN.name}?" +
                    "itemCode=${itemCode}&lat=${lat}&lng=${lng}&scheduleDate=${scheduleDate.seconds}&tripScheduleDocId=${tripScheduleDocId}")
    }

}