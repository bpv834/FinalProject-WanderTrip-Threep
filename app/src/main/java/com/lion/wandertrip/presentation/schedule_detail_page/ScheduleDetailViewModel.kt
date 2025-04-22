package com.lion.wandertrip.presentation.schedule_detail_page

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.util.ScheduleScreenName
import com.lion.wandertrip.vo.ScheduleItemVO
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripScheduleService: TripScheduleService
) : ViewModel() {
    val application = context as TripApplication

    // 선택한 도시
    val areaName = mutableStateOf("")
    // 선택한 도시 코드
    val areaCode = mutableIntStateOf(0)
    // 일정 문서 ID
    val tripScheduleDocId = mutableStateOf("")

    var selectedLocation = mutableStateOf<LatLng?>(null)
    var selectedDate = mutableStateOf<Timestamp?>(null)



    // val tripSchedule = TripScheduleModel()
    // 일정 데이터
    val tripSchedule = mutableStateOf(TripScheduleModel())
    val tripScheduleItems = mutableStateListOf<ScheduleItem>()

    val isLoading = mutableStateOf(true) // ✅ 로딩 상태 추가

    // Firestore 리스너 (변경 감지용)
    private var scheduleItemsListener: ListenerRegistration? = null

    // 서브컬렉션 데이터 변경 감지 및 UI 업데이트
    fun observeTripScheduleItems() {
        // Firestore 참조
        val firestore = FirebaseFirestore.getInstance()

        // 기존 리스너 제거 (중복 방지)
        scheduleItemsListener?.remove()

        // 새로운 리스너 추가 (데이터 변경 감지)
        scheduleItemsListener = firestore.collection("TripSchedule")
            .document(tripScheduleDocId.value)
            .collection("TripScheduleItem")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore Error", "❌ 서브컬렉션 리스닝 실패: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val updatedList = snapshot.documents.mapNotNull { it.toObject(ScheduleItem::class.java) }

                    // 데이터가 바뀌면 업데이트
                    tripScheduleItems.clear()
                    tripScheduleItems.addAll(updatedList)

                    Log.d("Firestore Update", "✅ 일정 항목 업데이트됨: ${tripScheduleItems.size}개")
                }
            }
    }

    // 일정 상세 정보 가져오기
    fun getTripSchedule() {
        viewModelScope.launch {
            // isLoading.value = true // ✅ 로딩 시작

            val work1 = async(Dispatchers.IO) {
                tripScheduleService.getTripSchedule(tripScheduleDocId.value)
            }.await()

            if (work1 != null) {
                tripSchedule.value = work1
            } else {
                Log.d("ScheduleViewModel", "해당 문서가 없습니다.")
            }

            // Firestore Snapshot Listener 활성화
            observeTripScheduleItems()

            delay(2000)
            isLoading.value = false // ✅ 로딩 완료
        }
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

    // Firestore 리스너 해제 (메모리 누수 방지)
    override fun onCleared() {
        super.onCleared()
        scheduleItemsListener?.remove()
    }

    // 도시 이름과 코드를 설정 하는 함수
    fun addAreaData(tripScheduleDocId: String, areaName: String, areaCode: Int) {
        this.tripScheduleDocId.value = tripScheduleDocId
        this.areaName.value = areaName
        this.areaCode.intValue = areaCode
    }

    // Timestamp를 "yyyy년 MM월 dd일" 형식의 문자열로 변환하는 함수
    fun formatTimestampToDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return sdf.format(Date(timestamp.seconds * 1000))
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

    // LazyColumn Reorderable 관련 //////////////////////////////////////////////////////////////////

    // 완료 버튼 클릭 시, 임시 리스트 기준으로 각 항목의 itemIndex를 1부터 업데이트
    fun updateItemsOrderForDate(tempList : SnapshotStateList<ScheduleItem>, date: Timestamp) {
        // 완료 버튼 클릭 시, 임시 리스트 기준으로 각 항목의 itemIndex를 1부터 업데이트
        tempList.forEachIndexed { index, item ->
            item.itemIndex = index + 1
        }
        // viewModel의 전체 리스트에서 해당 날짜 그룹만 새 순서로 반영
        tripScheduleItems.replaceAll { item ->
            if (item.itemDate.seconds == date.seconds) {
                tempList.find { it.itemDocId == item.itemDocId } ?: item
            } else {
                item
            }
        }
        // 전체 리스트를 날짜와 itemIndex 기준으로 정렬
        tripScheduleItems.sortWith(compareBy({ it.itemDate.seconds }, { it.itemIndex }))

        // 변경한 위치를 데이터베이스에 업데이트
        updateItemsPosition(tempList)
    }

    // 위치 조정한 일정 항목 업데이트
    fun updateItemsPosition(updatedItems: List<ScheduleItem>) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.updateItemsPosition(tripScheduleDocId.value, updatedItems)
            }.await()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // 화면 이동 관련 ////////////////////////////////////////////////////////////////////////////////

    // 일정 항목 선택 화면 으로 이동
    fun moveToScheduleSelectItemScreen(itemCode: Int, scheduleDate: Timestamp) {
        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_SELECT_ITEM_SCREEN.name}?" +
                    "itemCode=${itemCode}&areaName=${areaName.value}&areaCode=${areaCode.intValue}&scheduleDate=${scheduleDate.seconds}&tripScheduleDocId=${tripScheduleDocId.value}")
    }

    // 함께 하는 친구 목록으로 이동
    fun moveToScheduleDetailFriendsScreen(scheduleDocId: String) {
        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_FRIENDS_SCREEN.name}?" +
                    "scheduleDocId=${scheduleDocId}&userNickName=${tripSchedule.value.userNickName}"
        )
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

    // 이전 화면 으로 이동 (메인 일정 화면)
    fun backScreen() {
        application.navHostController.popBackStack()
    }
}