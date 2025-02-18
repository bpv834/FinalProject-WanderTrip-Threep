package com.lion.wandertrip.presentation.detail_page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.DetailModel
import com.lion.wandertrip.model.ReviewModel
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.presentation.detail_page.used_dummy_data.DetailDummyData
import com.lion.wandertrip.presentation.detail_page.used_dummy_data.ReviewDummyData
import com.lion.wandertrip.presentation.detail_page.used_dummy_data.TripScheduleDummyData
import com.lion.wandertrip.util.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject


@HiltViewModel
class DetailViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    val tripApplication = context as TripApplication

    // 소개 상태
    val isClickIntroState = mutableStateOf(true)

    // 기본 정보 상태
    val isClickBasicInfoState = mutableStateOf(false)

    // 후기 상태
    val isClickReviewState = mutableStateOf(false)

    // 컨텐트 모델
    val contentModelValue = mutableStateOf(DetailModel())

    // 리뷰 필터 시트 관리 상태 변수
    val isReviewOptionSheetOpen = mutableStateOf(false)

    //  일정 추가 시트
    val isAddScheduleSheetOpen = mutableStateOf(false)

    // 필터 스트링 상태 변수
    val filterStateStringValue = mutableStateOf("최신순")

    // 리뷰 리스트
    val reviewList = mutableStateListOf<ReviewModel>()

    // 필터된 리뷰 리스트
    val filteredReviewList = mutableStateListOf<ReviewModel>()

    // 일정 리스트
    val tripScheduleList = mutableStateListOf<TripScheduleModel>()

    // 정렬 상태 : 최신순
    val isRecentFilter = mutableStateOf(true)

    // 정렬 상태 : 별점 낮은순 , 별점 기준 오름차순
    val isRatingAsc = mutableStateOf(true)

    // 정렬 상태 : 별점 높은순 , 별점 기준 내림차순
    val isRatingDesc = mutableStateOf(true)

    // 이전에 눌렸던 메뉴 인덱스 상태
    var truedIdx = mutableStateOf(-1)

    // 메뉴 상태 관리 변수
    var isMenuOpened = mutableStateOf(false)

    // 인덱스별 메뉴 상태를 관리할 맵
    val menuStateMap = mutableStateMapOf<Int, Boolean>()

    // 이전에 눌렸던 날짜 인덱스 상태
    var scheduleDatePickerTruedIdx = mutableStateOf(-1)

    // 날짜 눌린게 있는지? 처음 누르는건지? 이전에 눌렀다면 트루
    var scheduleDatePickerIsClick = mutableStateOf(false)

    // 인덱스별 메뉴 상태를 관리할 맵
    val scheduleDatePickerStateMap = mutableStateMapOf<Int, Boolean>()

    // 일정추가 바텀시트 일정추가 아이콘 상태
    val isShownAddScheduleIconValue = mutableStateOf(false)





    // 리스트 길이로 맵을 초기화
    fun addMap() {
        tripScheduleList.forEachIndexed { index, tripScheduleModel ->
            menuStateMap[index] = false
        }
    }

    // 리스트 길이로 맵을 초기화
    fun addScheduleDatePickerMap(listSize: Int) {
        // 초기화 후 새롭게 담는다
        scheduleDatePickerStateMap.clear()
        for (i in 0 until listSize) {
            scheduleDatePickerStateMap[i] = false // 해당 인덱스에 false 값 초기화
        }
    }


    // Schedule 카드가 눌릴때
    fun onClickIconScheduleCard(clickPos: Int,scheduleDaysSize : Int) {
        // 날짜별 인덱스 bool 값 담는 맵 초기화
        addScheduleDatePickerMap(scheduleDaysSize)
        Log.d("test100"," map : $scheduleDatePickerStateMap")
        // 한번이라도 메뉴가 클릭된적이 없다면
        if (!isMenuOpened.value) {
            menuStateMap[clickPos] = true
            isMenuOpened.value = true
            truedIdx.value = clickPos

        } else {
            // 한번이상 메뉴가 클릭됐다면
            menuStateMap[truedIdx.value] = false
            menuStateMap[clickPos] = true
            truedIdx.value = clickPos
        }
        // 일정 추가 아이콘 숨기기
        isShownAddScheduleIconValue.value = false
    }

    // schedule 날짜 눌릴때 리스너
    fun onClickScheduleDateCard(clickPos: Int) {
        // 한번이라도 메뉴가 클릭된적이 없다면
        if (!scheduleDatePickerIsClick.value) {
            scheduleDatePickerStateMap[clickPos] = true
            scheduleDatePickerIsClick.value = true
            scheduleDatePickerTruedIdx.value = clickPos

        } else {
            // 한번이상 메뉴가 클릭됐다면
            scheduleDatePickerStateMap[scheduleDatePickerTruedIdx.value] = false
            scheduleDatePickerStateMap[clickPos] = true
            scheduleDatePickerTruedIdx.value = clickPos
        }
        // 일정 추가 아이콘 보이기
        isShownAddScheduleIconValue.value = true
    }

    // 일정 리스트 가져오기
    fun getTripSchedule() {
        tripScheduleList.addAll(
            TripScheduleDummyData.detailPageTripScheduleDummyData
        )
        addMap()
    }

    // 리뷰 리스트 가져오기
    fun getReviewModel() {
        reviewList.addAll(
            ReviewDummyData.detailReviewDataList
        )
    }

    // 리뷰 리스트 필터
    fun getFilteredReviewList() {
        filteredReviewList.clear()
        // 최신순 정렬
        if (isRecentFilter.value) {
            filteredReviewList.addAll(
                reviewList.sortedByDescending { it.reviewTimeStamp }
            )
        }
        // 별점 낮은순 정렬
        else if (isRatingAsc.value) {
            filteredReviewList.addAll(
                reviewList.sortedBy { it.reviewRatingScore }
            )

        }
        // 별점 높은순 정렬
        else {
            filteredReviewList.addAll(
                reviewList.sortedByDescending { it.reviewRatingScore }
            )
        }
    }

    // 컨텐트 ID 로 모델 가져오기
    fun getContentModel() {
        contentModelValue.value = DetailDummyData.detailDummyModel
    }

    // 소개 버튼 리스너
    fun onClickButtonIntro() {
        isClickIntroState.value = true
        isClickBasicInfoState.value = false
        isClickReviewState.value = false
    }

    // 기본 정보 리스너
    fun onClickButtonBasicInfo() {
        isClickIntroState.value = false
        isClickBasicInfoState.value = true
        isClickReviewState.value = false

    }

    // 후기 리스너
    fun onClickButtonReview() {
        isClickIntroState.value = false
        isClickBasicInfoState.value = false
        isClickReviewState.value = true

    }

    // 앱바의 로케이션 눌렀을때 리스너
    fun onClickIconMap() {
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_GOOGLE_MAP.name)
    }

    // 앱바 일정 추가 눌렀을 때 리스너
    fun onClickIconAddSchedule() {
        isAddScheduleSheetOpen.value = true
    }

    // 뒤로가기 리스너
    fun onClickNavIconBack() {
        tripApplication.navHostController.popBackStack()
    }

    // 홈페이지 따는 정규식
    fun getHomePage() {
        val inputString =
            """<a href="https://plaza.seoul.go.kr/?doing_wp_cron=1701728399.7732338905334472656250" target="_blank" title="새창 : 홈페이지로 이동">https://plaza.seoul.go.kr</a>"""
        val regex = "https://[\\S]+".toRegex()  // https:로 시작하는 URL을 찾는 정규식
        val match = regex.find(inputString)

        val homepageUrl = match?.value
        println(homepageUrl)  // "https://plaza.seoul.go.kr"
    }

    // 홈페이지 클릭 시 브라우저로 열기
    fun onClickTextHomepage(homepage: String) {
        if (homepage != "") {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(homepage))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // 플래그 추가 , context가 필요한데 context가 아닌 application으로 부르기때문에 플래그가 필요함
            tripApplication.startActivity(intent)
        }
    }

    // 전화 클릭 시 전화 앱 켜기
    fun onClickTextTel(telNum: String) {
        // 널체크
        if (telNum != "") {
            val number = telNum.replace("-", "")

            // 전화 앱을 여는 Intent 생성
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            // 플래그 추가 , context가 필요한데 context가 아닌 application으로 부르기때문에 플래그가 필요함
            dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // 전화 앱을 열기
            tripApplication.startActivity(dialIntent)
        }
    }

    // 리뷰 필터 텍스트 입력 리스너
    fun onClickTextReviewFilter() {
        isReviewOptionSheetOpen.value = true
    }

    // 리뷰 필터 최신순 눌렀을때
    fun onClickTextRecentFilter() {
        isRecentFilter.value = true
        isRatingAsc.value = false
        isRatingDesc.value = false
        filterStateStringValue.value = "최신순"
        getFilteredReviewList()
    }

    // 리뷰 필터 별점 낮은순 눌렀을때
    fun onClickTextRatingAsc() {
        isRecentFilter.value = false
        isRatingAsc.value = true
        isRatingDesc.value = false
        filterStateStringValue.value = "별점 낮은순"
        getFilteredReviewList()
    }

    // 리뷰 필터 별점 높은순 눌렀을때
    fun onClickTextRatingDesc() {
        isRecentFilter.value = false
        isRatingAsc.value = false
        isRatingDesc.value = true
        filterStateStringValue.value = "별점 높은순"
        getFilteredReviewList()

    }

    // 리뷰 쓰기 버튼 리스너
    fun onClickIconReviewWrite(contentID: String, contentTitle: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL_REVIEW_WRITE.name}/${contentID}/${contentTitle}")
    }

    // 리뷰 수정 버튼 리스너
    fun onClickIconReviewModify(contentID: String, reviewDocID: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL_REVIEW_MODIFY.name}/${contentID}/${reviewDocID}")
    }

    // timeStamp -> String 변환
    fun convertToMonthDate(timeStamp: Timestamp): String {
        // Firestore Timestamp를 Date 객체로 변환
        val date = timeStamp.toDate()

        // 한국 시간대 (Asia/Seoul)로 설정
        val dateFormat = SimpleDateFormat("yyyy MM월", Locale.KOREA)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return dateFormat.format(date)
    }

    // timeStamp -> String 변환
    fun convertToDate(timeStamp: Timestamp): String {
        // Firestore Timestamp를 Date 객체로 변환
        val date = timeStamp.toDate()

        // 한국 시간대 (Asia/Seoul)로 설정
        val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return dateFormat.format(date)
    }

    // timeStamp -> String 변환
    fun convertToMonthDayDate(timeStamp: Timestamp): String {
        // Firestore Timestamp를 Date 객체로 변환
        val date = timeStamp.toDate()

        // 한국 시간대 (Asia/Seoul)로 설정
        val dateFormat = SimpleDateFormat("MM. dd", Locale.KOREA)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return dateFormat.format(date)
    }
    // TimeStamp -> 요일 출력
    fun convertToDayOfWeekFromTimestamp(timestamp: Timestamp): String {
        // Timestamp를 Date로 변환
        val date = timestamp.toDate()

        // SimpleDateFormat을 사용하여 요일 추출
        val sdf = SimpleDateFormat("EEE", Locale.KOREAN)
        val dayOfWeek = sdf.format(date)

        return when (dayOfWeek) {
            "월" -> "월"
            "화" -> "화"
            "수" -> "수"
            "목" -> "목"
            "금" -> "금"
            "토" -> "토"
            "일" -> "일"
            else -> "알 수 없음"
        }
    }
}