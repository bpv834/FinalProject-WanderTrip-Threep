package com.lion.wandertrip.presentation.trip_note_detail_page

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.model.TripNoteReplyModel
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.util.TripNoteScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class TripNoteDetailViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripNoteService : TripNoteService,
    val tripScheduleService : TripScheduleService
) : ViewModel() {

    // 여행기 정보를 담을 변수
    val tripNoteModelValue = mutableStateOf(TripNoteModel())

    // 여행기 제목
    val textFieldTripNoteSubject = mutableStateOf(" ")
    // 여행기 작성자 닉네임
    val textFieldTripNoteNickName = mutableStateOf(" ")
    // 여행기 내용
    val textFieldTripNoteContent = mutableStateOf(" ")
    // 일정 문서 id
    val textFieldTripNoteScheduleDocId = mutableStateOf(" ")

    // 스크랩 수
    val textFieldTripNoteScrap = mutableStateOf(" ")

    // 휴지통 아이콘을 보여줄 것인지에 대한 상태 변수
    val showTopAppBarDeleteMenuState = mutableStateOf(false)
    // 이미지 요소를 띄울 것인지에 대한 상태 변수
    val showImageState = mutableStateOf(false)

    // 작성한 댓글 내용
    val textFieldTripNoteReply = mutableStateOf("")
    // 댓글 리스트
    var tripNoteReplyList = mutableStateListOf<TripNoteReplyModel>()

    val tripApplication = context as TripApplication
    val nickName = tripApplication.loginUserModel.userNickName

    val tripSchedule = mutableStateOf(TripScheduleModel())
    var tripScheduleItems = mutableStateListOf<ScheduleItem>()

    val isLoading = mutableStateOf(false) // ✅ 로딩 상태 추가


    // 일정 상세 정보 가져오기
    suspend fun getTripSchedule() {
        Log.d("ScheduleViewModel", "getTripSchedule() 호출됨")

            isLoading.value = true
            Log.d("ScheduleViewModel", "로딩 시작")

            val work1 = tripNoteService.getTripSchedule(textFieldTripNoteScheduleDocId.value)
            // 첫 번째 작업: 일정 정보 가져오기
            Log.d("ScheduleViewModel", "일정 문서 ID: ${textFieldTripNoteScheduleDocId.value}")

            if (work1 != null) {
                tripSchedule.value = work1
                Log.d("ScheduleViewModel", "일정 정보 가져오기 완료: $work1")
            } else {
                Log.d("ScheduleViewModel", "해당 일정 문서가 없습니다.")
            }

            // 두 번째 작업: 일정 아이템들 가져오기
            val work2 = tripNoteService.getTripScheduleItems(textFieldTripNoteScheduleDocId.value)

            if (work2 != null) {
                tripScheduleItems.clear()
                tripScheduleItems.addAll(work2)
                Log.d("ScheduleViewModel", "일정 아이템 가져오기 완료. 아이템 수: ${tripScheduleItems.size}")
            } else {
                Log.d("ScheduleViewModel", "해당 일정 아이템 문서가 없습니다.")
            }

            isLoading.value = false
            Log.d("ScheduleViewModel", "로딩 완료")

    }


    var tripNoteDetailList = mutableStateListOf<TripNoteModel>()
    val areaName = mutableStateOf("")
    val areaCode = mutableIntStateOf(0)

    // 테스트 용 데이터 ///////////////////////////////////////////////////////////////////////////////
    val startDate = Timestamp(1738627200, 0)
    val endDate = Timestamp(1738886400, 0)
    val scheduleDateList = generateDateList(startDate, endDate)




    // 여행기 리사이클러뷰 데이터 가져오기
    fun gettingTripNoteDetailData(documentId: String) {
        Log.d("TripNote", "gettingTripNoteDetailData 호출됨. documentId: $documentId")

        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                Log.d("TripNote", "서버에서 여행기 데이터 조회 시작")
                val result = tripNoteService.selectTripNoteDataOneById(documentId)
                Log.d("TripNote", "서버에서 여행기 데이터 조회 완료: $result")
                result
            }

            tripNoteModelValue.value = work1.await()!!

            Log.d("TripNote", "tripNoteModel 데이터 할당 완료")

            textFieldTripNoteNickName.value = tripNoteModelValue.value.userNickname
            textFieldTripNoteSubject.value = tripNoteModelValue.value.tripNoteTitle
            textFieldTripNoteContent.value = tripNoteModelValue.value.tripNoteContent
            textFieldTripNoteScheduleDocId.value = tripNoteModelValue.value.tripScheduleDocumentId
            textFieldTripNoteScrap.value = tripNoteModelValue.value.tripNoteScrapCount.toString()

            Log.d("TripNote", "텍스트 필드에 값 할당 완료")

            // 일정 문서 아이디 할당
            textFieldTripNoteScheduleDocId.value = tripNoteModelValue.value.tripScheduleDocumentId

            // 일정 가져오기
            getTripSchedule()

            if (tripNoteModelValue.value.userNickname == tripApplication.loginUserModel.userNickName) {
                showTopAppBarDeleteMenuState.value = true
                Log.d("TripNote", "작성자와 로그인 사용자 일치 → 삭제 메뉴 표시")
            } else {
                Log.d("TripNote", "작성자와 로그인 사용자 불일치 → 삭제 메뉴 미표시")
            }
            if (tripNoteModelValue.value.tripNoteImage.isNotEmpty()) {
                Log.d("TripNote", "이미지 경로 존재: ${tripNoteModelValue.value.tripNoteImage}")

            } else {
                Log.d("TripNote", "첨부된 이미지 없음")
            }
        }
    }

    // 댓글 등록하기 버튼
    fun addReplyClick(tripNoteDocId : String,replyDocumentId : String){
        // 댓글 작성한 여행기 문서 id
        val tripNoteDocumentId = replyDocumentId
        // 작성자 닉네임
        val userNickname = nickName
        // 댓글 내용
        val replyText = textFieldTripNoteReply.value
        // 댓글 작성 시간
        val replyTimeStamp: Timestamp = Timestamp.now()

        // 서버에 저장할 댓글 데이터
        val tripNoteReplyModel = TripNoteReplyModel()
        tripNoteReplyModel.tripNoteDocumentId = tripNoteDocumentId
        tripNoteReplyModel.userNickname = userNickname
        tripNoteReplyModel.replyText = replyText
        tripNoteReplyModel.replyTimeStamp = replyTimeStamp

        // 저장하기
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                tripNoteService.addTripNoteReplyData(tripNoteDocId,tripNoteReplyModel)
            }
            work1.join()

        }
    }

    // 댓글 리스트 데이터 가져오기
    fun gettingTripNoteReplyData(tripNoteDocumentId : String) {
        // 서버에서 데이터를 가져온다.
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                tripNoteService.selectReplyDataOneById(tripNoteDocumentId)
            }
            val result = work1.await()
            tripNoteReplyList.clear()
            tripNoteReplyList.addAll(result)
        }
    }

    // 댓글 삭제하기 다이얼로그 확인 버튼 누르면,,
    fun deleteTripNoteReply(tripNoteReplyDocId : String){
        CoroutineScope(Dispatchers.Main).launch {

            // 댓글을 삭제한다.
            val work1 = async(Dispatchers.IO){
                tripNoteService.deleteReplyData(tripNoteReplyDocId)
            }
            work1.join()
        }

    }

    // 뒤로 가기 버튼
    fun navigationButtonClick(){
        tripApplication.navHostController.popBackStack()
    }

    // 휴지통 아이콘 (여행기 삭제)
    fun deleteButtonClick(documentId : String){
        // 삭제 그거 ... 다이얼로그 띄우고 확인 누르면,,,
        CoroutineScope(Dispatchers.Main).launch {
            // 여행기를 삭제한다.
            val work1 = async(Dispatchers.IO){
                tripNoteService.deleteTripNoteData(documentId)
            }
            work1.join()

            tripApplication.navHostController.popBackStack()

        }

    }

    // 일정 담기 아이콘 해당 여행기의 일정 문서id와 여행기 문서id를 전달해줌

    // 일정 담기 아이콘 해당 여행기의 일정 문서id와 여행기 문서id를 전달해줌
    fun bringTripNote(tripNoteScheduleDocId: MutableState<String>, documentId: String) {
        val tripNoteScheduleDocIdValue = tripNoteScheduleDocId.value

        Log.d("BringTripNote", "tripNoteScheduleDocId: $tripNoteScheduleDocIdValue")
        Log.d("BringTripNote", "documentId: $documentId")

        tripApplication.navHostController.navigate(
            "${TripNoteScreenName.TRIP_NOTE_SELECT_DOWN.name}/$tripNoteScheduleDocIdValue/$documentId"
        )
    }


    // 닉네임 클릭하면 그 사람 여행기 리스트 화면으로 이동
    fun clickNickname(){
        val otherNickName = textFieldTripNoteNickName.value
        tripApplication.navHostController.navigate("${TripNoteScreenName.TRIP_NOTE_OTHER_SCHEDULE.name}/${otherNickName}")
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

    // 도시 이름과 코드를 설정 하는 함수
    fun addAreaData(tripScheduleDocId: String, areaName: String, areaCode: Int) {
        this.textFieldTripNoteScheduleDocId.value = tripScheduleDocId
        this.areaName.value = areaName
        this.areaCode.intValue = areaCode
    }

    // Timestamp를 "yyyy년 MM월 dd일" 형식의 문자열로 변환하는 함수
    fun formatTimestampToDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return sdf.format(Date(timestamp.seconds * 1000))
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