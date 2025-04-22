package com.lion.wandertrip.presentation.trip_note_select_down_page

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.util.AreaCode
import com.lion.wandertrip.util.ScheduleScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject


@HiltViewModel
class TripNoteSelectDownViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripNoteService: TripNoteService,
) : ViewModel() {

    // 내 일정
    var tripNoteMyScheduleList = mutableStateListOf<TripScheduleModel?>()

    val tripApplication = context as TripApplication
    val userNickName = tripApplication.loginUserModel.userNickName

    // 클릭 일정 문서id
    var scheduleDocId = mutableStateOf("")

    // 기존 일정 그 지역 이름
    var scheduleCity : String = ""
    // 지역 코드
    var areaCode : Int = 0

    // 여행기 상세 데이터를 가져오는 함수
    fun gettingTripNoteDetailData() {

        Log.d("GettingTripNote", "getTripNoteDetailData() called")

        CoroutineScope(Dispatchers.Main).launch {
            Log.d("GettingTripNote", "Coroutine started on Main thread")

            val work1 = async(Dispatchers.IO) {
                Log.d("GettingTripNote", "Fetching upcoming schedule list on IO thread for user: ${tripApplication.loginUserModel.userDocId}")
                tripNoteService.gettingUpcomingScheduleListByUserDocId(tripApplication.loginUserModel.userDocId)
            }

            val recyclerViewList = work1.await()
            Log.d("GettingTripNote", "Received schedule list: ${recyclerViewList.size} items")

            // 상태 관리 변수에 담아준다.
            tripNoteMyScheduleList.clear()
            tripNoteMyScheduleList.addAll(recyclerViewList)
            Log.d("GettingTripNote", "tripNoteMyScheduleList updated with new data")
        }
    }


    // 뒤로 가기 버튼
    fun navigationButtonClick(){
        tripApplication.navHostController.popBackStack()
    }

    // 새 일정에 담기
    fun goScheduleTitleButtonClick(tripNoteScheduleDocId : String, documentId : String){
        tripApplication.navHostController.popBackStack()
        tripApplication.navHostController.popBackStack()
        // 일정 제목 + 날짜 선택 입력 화면으로 이동 (documentId 전달)
        tripApplication.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_ADD_SCREEN.name}/$tripNoteScheduleDocId"
        )

        // 일정 담기면 그 여행기의 tripNoteScrapCount 증가시키기
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){
                tripNoteService.addTripNoteScrapCount(documentId)
            }
            work1.join()
        }
    }




    // 다가오는 일정 클릭하면,, 그 일정 문서id 받기
    fun gettingSelectId(tripScheduleDocId: String){
        scheduleDocId.value = tripScheduleDocId
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

    // 다이얼로그 상태 관리
    val showDialogState = mutableStateOf(false)


    // 다이얼로그 확인 버튼 클릭 시 동작
    fun onConfirmClick() {
        showDialogState.value = false
    }


    // 기존 일정에 담기 버튼
    fun selectFinishButtonClick(
        tripNoteScheduleDocId: String,
        scheduleDocId: String,
        documentId: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val tripNoteScheduleRef = firestore.collection("TripSchedule").document(tripNoteScheduleDocId)
        val scheduleRef = firestore.collection("TripSchedule").document(scheduleDocId)

        tripNoteScheduleRef.get().addOnSuccessListener { originalScheduleDoc ->
            if (!originalScheduleDoc.exists()) {
                Log.e("Firestore", "❌ 원본 일정 문서를 찾을 수 없음.")
                return@addOnSuccessListener
            }

            val originalStartDate = (originalScheduleDoc.get("scheduleStartDate") as? Timestamp)?.seconds ?: return@addOnSuccessListener
            val originalEndDate = (originalScheduleDoc.get("scheduleEndDate") as? Timestamp)?.seconds ?: return@addOnSuccessListener

            tripNoteScheduleRef.collection("TripScheduleItem").get().addOnSuccessListener { snapshot ->
                val itemCountByDate = mutableMapOf<Long, Int>()

                snapshot.documents.forEach { document ->
                    val itemData = document.data?.toMutableMap() ?: return@forEach
                    val itemDate = (itemData["itemDate"] as? Timestamp)?.seconds ?: return@forEach

                    // 날짜별로 아이템 개수를 세는 부분
                    val adjustedItemDateOnly = getDateOnly(itemDate)
                    itemCountByDate[adjustedItemDateOnly] = itemCountByDate.getOrDefault(adjustedItemDateOnly, 0) + 1
                }

                scheduleRef.get().addOnSuccessListener { newScheduleDoc ->
                    val newStartDate = (newScheduleDoc.get("scheduleStartDate") as? Timestamp)?.seconds ?: return@addOnSuccessListener
                    val newEndDate = (newScheduleDoc.get("scheduleEndDate") as? Timestamp)?.seconds ?: return@addOnSuccessListener

                    // 지역 이름, 지역 코드 뽑기
                    scheduleCity = newScheduleDoc.getString("scheduleCity") ?: return@addOnSuccessListener
                    // scheduleCity 값에 해당하는 areaCode를 찾는 방법
                    areaCode = AreaCode.values().find { it.areaName == scheduleCity }?.areaCode!!

                    // 새 일정의 날짜별 문서 개수를 세기 위한 맵
                    val newItemCountByDate = mutableMapOf<Long, Int>()
                    scheduleRef.collection("TripScheduleItem").get().addOnSuccessListener { newSnapshot ->
                        newSnapshot.documents.forEach { newDocument ->
                            val newItemData = newDocument.data?.toMutableMap() ?: return@forEach
                            val newItemDate = (newItemData["itemDate"] as? Timestamp)?.seconds ?: return@forEach

                            // 새 일정에서 날짜별로 문서 개수를 세는 부분
                            val adjustedNewItemDateOnly = getDateOnly(newItemDate)
                            newItemCountByDate[adjustedNewItemDateOnly] = newItemCountByDate.getOrDefault(adjustedNewItemDateOnly, 0) + 1
                        }

                        // 원본 일정 아이템 갱신
                        snapshot.documents.forEach { document ->
                            val itemData = document.data?.toMutableMap() ?: return@forEach
                            val itemDate = (itemData["itemDate"] as? Timestamp)?.seconds ?: return@forEach

                            // 날짜 변환
                            val adjustedItemDate = getAdjustedItemDate(originalStartDate, originalEndDate, itemDate, newStartDate, newEndDate)

                            // 새 일정 날짜에 맞춰서 인덱스를 차례대로 매기기
                            val adjustedItemDateOnly = getDateOnly(adjustedItemDate)
                            val newItemCount = newItemCountByDate.getOrDefault(adjustedItemDateOnly, 0)



                            // 원본 일정 아이템의 itemIndex 설정
                            itemData["itemDate"] = Timestamp(adjustedItemDate, 0)
                            itemData["itemIndex"] = newItemCount + 1
                            // itemIndex를 역순으로 적용 (아이템 인덱스를 새로 추가할 때 내림차순으로 넣음)
                            // itemData["itemIndex"] = newItemCountByDate[adjustedItemDateOnly]?.let { it + 1 } ?: 1

                            // 기존의 itemDocId 제거
                            itemData.remove("itemDocId")

                            // 원본 일정 아이템 업데이트
                            scheduleRef.collection("TripScheduleItem").add(itemData).addOnSuccessListener { newDocument ->
                                scheduleRef.collection("TripScheduleItem").document(newDocument.id)
                                    .update("itemDocId", newDocument.id).addOnSuccessListener {
                                        Log.d("Firestore", "✅ 원본 일정 아이템 추가 완료: ${newDocument.id}")
                                    }
                            }

                            // 새 일정 날짜별 문서 개수 갱신
                            newItemCountByDate[adjustedItemDateOnly] = newItemCount + 1
                        }

                        tripApplication.navHostController.popBackStack()
                        tripApplication.navHostController.popBackStack()

                        // 일정 상세 화면으로 이동 - newScheduleDoc.id(새로 만들어진 일정 문서 id) 전달
                        tripApplication.navHostController.navigate(
                            "${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
                                    "tripScheduleDocId=${scheduleDocId}&areaName=${scheduleCity}&areaCode=${areaCode}"
                        )

                        Log.d("Schedule11", "지역 이름: $scheduleCity, 지역 코드: $areaCode")
                    }
                }
            }
        }


        // 일정 담기면 그 여행기의 tripNoteScrapCount 증가시키기
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){
                tripNoteService.addTripNoteScrapCount(documentId)
            }
            work1.join()
        }
    }


    // 날짜 변환 함수
    fun getAdjustedItemDate(
        originalStartDate: Long,
        originalEndDate: Long,
        itemDate: Long,
        newStartDate: Long,
        newEndDate: Long
    ): Long {
        // 원본 일정의 itemDate를 새 일정의 기간에 맞게 변환
        var adjustedItemDate = newStartDate + (itemDate - originalStartDate)

        // 새 일정의 마지막 날짜를 초과하는 경우, 새 일정의 마지막 날짜로 맞추기
        if (adjustedItemDate > newEndDate) {
            adjustedItemDate = newEndDate
        }

        return adjustedItemDate
    }



    // ✅ 날짜만 추출
    fun getDateOnly(timestamp: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+9"))
        calendar.timeInMillis = timestamp * 1000
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis / 1000
    }




    // 새 일정 담기 버튼 다이얼로그 취소 버튼 클릭 시 동작
    fun onDismissClick() {
        showDialogState.value = false
    }


    // 다이얼로그 상태 관리
    val showDialogNotState = mutableStateOf(false)

    // 새 일정 담기 버튼 다이얼로그 취소 버튼 클릭 시 동작
    fun onDismissNotClick() {
        showDialogNotState.value = false
    }


    // 다이얼로그 확인 버튼 클릭 시 동작
    fun onConfirmNotClick() {
        showDialogNotState.value = false
    }


    // 다이얼로그 상태 관리
    val showDialogStateNew = mutableStateOf(false)



    // 새 일정 담기 버튼 클릭 시 다이얼로그 상태 변경
    fun selectNewButtonClick() {
        showDialogStateNew.value = true
    }

    // 새 일정 담기 버튼 다이얼로그 확인 버튼 클릭 시 동작
    fun onConfirmNewClick() {
        showDialogStateNew.value = false
        // 일정 제목 그거로 이동
    }

    // 새 일정 담기 버튼 다이얼로그 취소 버튼 클릭 시 동작
    fun onDismissNewClick() {
        showDialogStateNew.value = false
    }

}