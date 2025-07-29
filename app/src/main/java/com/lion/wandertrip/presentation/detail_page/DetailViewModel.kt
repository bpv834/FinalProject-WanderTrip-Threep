package com.lion.wandertrip.presentation.detail_page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.model.RecentTripItemModel
import com.lion.wandertrip.model.ReviewModel
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.model.TripCommonItem
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.ContentsReviewService
import com.lion.wandertrip.service.ContentsService
import com.lion.wandertrip.service.TripCommonItemService
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.ContentTypeId
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.Tools
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject


@HiltViewModel
class DetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val tripCommonItemService: TripCommonItemService,
    val contentsReviewService: ContentsReviewService,
    val contentsService: ContentsService,
    val tripScheduleService: TripScheduleService,
    val userService: UserService,
) : ViewModel() {
    val tripApplication = context as TripApplication

    //  일정 추가 시트
    val isAddScheduleSheetOpen = mutableStateOf(false)

    // api 로 받아온 content 모델
    val tripCommonContentModelValue = mutableStateOf(TripCommonItem())

    // db 에서 가져온 content ( 별점과, 별점 등록 인원 파악 위함)
    val contentValue = mutableStateOf(ContentsModel())

    // 일정 리스트
    val tripScheduleList = mutableStateListOf<TripScheduleModel>()

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

    // 로딩 상태 변수
    val isLoading = mutableStateOf(false)

    // 토스트 메시지 value
    val toastMessageValue = mutableStateOf<String>("")

    // 상태 변경 메서드
    fun setState(value: Boolean) {
        //Log.d("test100", "state : $isLoading")
        isLoading.value = value
        //Log.d("test100", "state : $isLoading")
    }

    // 컨텐트 ID 로 api 에서 받아온  모델 가져오기
    fun getCommonTripContentModel(contentID: String) {
        setState(true)
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripCommonItemService.getTripCommonItem(contentID, null)
            }
            tripCommonContentModelValue.value = work1.await()!!

            // 지역이름 받기
            gettingCityName(
                tripCommonContentModelValue.value.areaCode ?: "",
                tripCommonContentModelValue.value.siGunGuCode ?: "",
            )

            // 최근 본 목록에 추가하기
            addRecentItem(
                contentID = tripCommonContentModelValue.value.contentId!!,
                contentType = tripCommonContentModelValue.value.contentTypeId!!,
                imageUri = tripCommonContentModelValue.value.firstImage.toString(),
                title = tripCommonContentModelValue.value.title!!
            )

            setState(false)
        }
    }

    // 리스트 길이로 맵을 초기화 메서드
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

    // 앱바의 로케이션 눌렀을때 리스너
    fun onClickIconMap(contentID: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_GOOGLE_MAP.name}/$contentID")
    }

    // 앱바 일정 추가 눌렀을 때 리스너
    fun onClickIconAddSchedule() {
        isAddScheduleSheetOpen.value = true
    }

    // 뒤로가기 리스너
    fun onClickNavIconBack() {
        tripApplication.navHostController.popBackStack()
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


    // Schedule 카드가 눌릴때 메서드
    fun onClickIconScheduleCard(clickPos: Int, scheduleDaysSize: Int) {
        // 날짜별 인덱스 bool 값 담는 맵 초기화
        addScheduleDatePickerMap(scheduleDaysSize)
        // Log.d("test100", " map : $scheduleDatePickerStateMap")
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

    // schedule 날짜 눌릴때 리스너 메서드
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
        tripScheduleList.clear()
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.gettingMyTripSchedules(tripApplication.loginUserModel.userNickName)
            }
            val result = work1.await()
            tripScheduleList.addAll(result)
            addMap()
        }
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

    // 최근 본 목록에 추가하기
    fun addRecentItem(
        contentID: String,
        contentType: String,
        title: String,
        imageUri: String,
    ) {
        val recentModel = RecentTripItemModel(
            title = title,
            contentTypeID = contentType,
            imageUri = imageUri,
            contentID = contentID,
        )
        Log.d("addRecentItem", "$recentModel")
        Tools.addRecentItemList(tripApplication, recentModel)
    }

    // 토스트 메시지 띄우는 메서드
    fun triggerToast(message: String) {
        toastMessageValue.value = message
    }

// --------------------------------------------------------------------------------------------
    // 소개관련

    // 소개 cityName
    val cityNameValue = mutableStateOf("")

    // 소개 상태
    val isClickIntroState = mutableStateOf(true)

    // 별점 등록 문서 개수
    val reviewCountValue = mutableStateOf(0)

    // 유저 좋아요 목록에 해당 페이지 있는가를 나타내는 상태변수
    val isLikeContentValue = mutableStateOf(false)


    // 코드로 지역 이름 가져오기
    fun gettingCityName(areaCode: String, siGunGuCode: String) {
        Log.d("test", "areaCode : $areaCode")
        cityNameValue.value = Tools.getAreaDetails(areaCode, siGunGuCode)
    }

    // 좋아요 목록에 해당 글이 있는가?
    fun isLikeContent(contentId: String) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                userService.isLikeContent(tripApplication.loginUserModel.userDocId, contentId)
            }
            isLikeContentValue.value = work1.await()
        }
    }

    // 좋아요 버튼 누를때 리스너 메서드
    fun onClickIconIsLikeContent(contentId: String) {
        Log.d("init","좋아요 눌림")
        val nav = tripApplication.navHostController
        viewModelScope.launch {
            // 좋아요상태를 변경한다.
            val work1 = async(Dispatchers.IO) {
                when (isLikeContentValue.value) {
                    // 좋아요 취소
                    true -> {
                        userService.removeLikeItem(
                            tripApplication.loginUserModel.userDocId,
                            contentId
                        )
                        userService.removeLikeCnt(contentId)
                    }
                    // 좋아요 추가
                    false -> {
                        userService.addLikeItem(
                            tripApplication.loginUserModel.userDocId,
                            contentId
                        )

                        userService.addLikeCnt(contentId)
                    }
                }
            }
            work1.join()
            // 변경 상태를 화면에 반영한다
            isLikeContent(contentId)
            // 이전 스택에 refresh_needed = true 를 저장
            // 내관심 -> 아이템 디테일 일때 변경사항이 있다면 리스트를 리로드하기위한 로직
            nav.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_needed", true)

        }

    }

    // 컨텐트 ID 로 DB 에서 모델 가져오기
    fun getContentModel(contentID: String) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                contentsService.getContentByContentsId(contentID)
            }
            contentValue.value = work1.await()

            val work2 = async(Dispatchers.IO) {
                getReviewCount()
            }
            reviewCountValue.value = work2.await()
            Log.d("test", "content Rating : ${contentValue.value.ratingScore}")

        }
    }

    // 컨텐츠의 리뷰 개수 가져오기(별점을 얼마나 많은 유저가 등록했는가 알아보기 위함)
    suspend fun getReviewCount(): Int {
        return contentsReviewService.getAllReviewsCountWithContents(contentValue.value.contentId)
    }

    // 내일정에 지금 컨텐츠 추가하기
    fun addSchedule(
        tripScheduleDocId: String,
        title: String,
        type: String,
        date: Timestamp,
        lat: Double,
        lng: Double,
        contentId: String
    ) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                val scheduleItem = ScheduleItem(
                    itemTitle = title,
                    itemType = when (type) {
                        ContentTypeId.TOURIST_ATTRACTION.contentTypeCode.toString() -> "관광지"
                        ContentTypeId.RESTAURANT.contentTypeCode.toString() -> "음식점"
                        ContentTypeId.ACCOMMODATION.contentTypeCode.toString() -> "숙소"
                        else -> ""
                    },
                    itemDate = date,
                    itemLatitude = lat,
                    itemLongitude = lng,
                    itemContentId = contentId,
                )

                tripScheduleService.addTripItemToSchedule(
                    tripScheduleDocId, date, scheduleItem
                )
            }.join()
            isAddScheduleSheetOpen.value = false
            // 토스트 표시
            triggerToast("일정에 추가되었습니다.") // 토스트 메시지 보내기
        }
    }


// --------------------------------------------------------------------------------------------
    // 기본정보 관련

    // 기본 정보 상태
    val isClickBasicInfoState = mutableStateOf(false)

    fun getHomePage(inputStr: String): String {
        val regex = "https://[\\S]+(?=\")".toRegex()  // https://로 시작하고 "로 끝나는 URL을 찾는 정규식
        val match = regex.find(inputStr)

        return match?.value ?: ""
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

// --------------------------------------------------------------------------------------------
    // 후기관련

    // 후기 상태
    val isClickReviewState = mutableStateOf(false)


    // 리뷰 필터 바텀 시트 관리 상태 변수
    val isReviewOptionSheetOpen = mutableStateOf(false)


    // 필터 스트링 상태 변수
    val filterStateStringValue = mutableStateOf("최신순")

    // 리뷰 리스트
    val reviewList = mutableStateListOf<ReviewModel>()

    // 필터된 리뷰 리스트
    val filteredReviewList = mutableStateListOf<ReviewModel>()

    // 정렬 상태 : 최신순
    val isRecentFilter = mutableStateOf(true)

    // 정렬 상태 : 별점 낮은순 , 별점 기준 오름차순
    val isRatingAsc = mutableStateOf(true)

    // 정렬 상태 : 별점 높은순 , 별점 기준 내림차순
    val isRatingDesc = mutableStateOf(true)

    // 리뷰 사진 관리 map
    val reviewImageUrlMap = mutableStateMapOf<Int, List<Uri>>()

    fun getReviewList() {
        reviewList.clear()
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                contentsReviewService.getAllReviewsWithContents(tripCommonContentModelValue.value.contentId!!)
            }

            reviewList.addAll(list)

            // 데이터 로딩이 완료된 후 필터링 실행
            getFilteredReviewList()
        }
    }

    // 리뷰 리스트 필터
    fun getFilteredReviewList() {
        filteredReviewList.clear()
        // 최신순 정렬
        if (isRecentFilter.value) {
            filteredReviewList.addAll(reviewList.sortedByDescending { it.reviewTimeStamp })
        }
        // 별점 낮은순 정렬
        else if (isRatingAsc.value) {
            filteredReviewList.addAll(reviewList.sortedBy { it.reviewRatingScore })
        }
        // 별점 높은순 정렬
        else {
            filteredReviewList.addAll(reviewList.sortedByDescending { it.reviewRatingScore })
        }
    }

    // 이미지 uri로 변경
    fun getUri(filterReviewList: MutableList<ReviewModel>) {
        // url map 초기화
        reviewImageUrlMap.clear()
        // 로그 추가: 함수가 호출되었는지 확인
        //Log.d("getUri", "getUri() 함수 호출됨, 이미지 URL 가져오기 시작")

        viewModelScope.launch {
            filterReviewList.forEachIndexed { index, reviewModel ->
                //Log.d("getUri", "$index 번째 리뷰에 대한 이미지 URL 가져오기 시작")

                // 이미지 URL 리스트 가져오기
                val urlList = contentsReviewService.gettingReviewImage(
                    filterReviewList[index].reviewImageList.toList(),
                    tripCommonContentModelValue.value.contentId!!
                )

                // 로그: URL 리스트가 어떻게 나왔는지 확인
                //Log.d("getUri", "$index 번째 리뷰의 URL 리스트: $urlList")

                // URL 맵에 추가
                reviewImageUrlMap[index] = urlList.toMutableStateList()
                //Log.d("test100", "map[$index] : $reviewImageUrlMap[$index]")
            }

            // 로그: 작업이 끝났는지 확인
            Log.d("getUri", "모든 이미지 URL 가져오기 완료")

            // 로딩 종료
            isLoading.value = false
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
    fun onClickIconReviewModify(contentsID: String, reviewDocID: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL_REVIEW_MODIFY.name}/${contentsID}/${reviewDocID}")
    }


    // 리뷰 삭제 버튼
    fun deleteReview(contentDocId: String, contentsReviewDocId: String) {
        //Log.d("test100", "contentDocId :$contentDocId, contentsReviewDocId:$contentsReviewDocId")
        viewModelScope.launch {
            // 삭제 진행
            val work1 = async(Dispatchers.IO) {
                contentsReviewService.deleteContentsReview(contentsReviewDocId)
            }
            work1.join()
            // 컨텐츠 별점 수정
            val work2 = async(Dispatchers.IO) {
                contentsService.updateContentRatingAndRatingCount(contentDocId)
            }
            work2.join()

            getReviewList()
        }

    }

    // 유저 리뷰 개수 가져오기
    suspend fun getCountUserReview(nickName: String): Int {
        return contentsReviewService.getCountUserReview(nickName)
    }


}