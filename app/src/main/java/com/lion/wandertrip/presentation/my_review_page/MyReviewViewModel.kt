package com.lion.wandertrip.presentation.my_review_page

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ReviewModel
import com.lion.wandertrip.service.ContentsReviewService
import com.lion.wandertrip.service.ContentsService
import com.lion.wandertrip.util.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MyReviewViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val contentsReviewService: ContentsReviewService,
    val contentsService: ContentsService,
) : ViewModel() {

    // 인덱스별 메뉴 상태를 관리할 맵
    val menuStateMap = mutableStateMapOf<Int, Boolean>()

    // 이전에 눌렀던 메뉴 인덱스
    var truedIdx = mutableStateOf(-1)

    // 메뉴 상태 관리 변수
    var isMenuOpened = mutableStateOf(false)
    val tripApplication = context as TripApplication

    val isLoading = mutableStateOf(false)

    val reviewList = mutableStateListOf<ReviewModel>()

    // 리뷰 가져오는 메서드
    fun getReviewList() {
        reviewList.clear()
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                contentsReviewService.getContentsUserReviewByNickName(tripApplication.loginUserModel.userNickName)
            }
            val result = work1.await()
            reviewList.addAll(result)
            addMap()
            isLoading.value = false

        }
    }

    // 리스트 길이로 맵을 초기화
    fun addMap() {
        menuStateMap.clear()
        reviewList.forEachIndexed { index, tripNoteModel ->
            menuStateMap[index] = false
        }
    }

    // 수정 버튼 리스너 메서드
    fun onClickIconEditReview(contentsID: String, reviewDocID: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL_REVIEW_MODIFY.name}/${contentsID}/${reviewDocID}")
    }

    // 삭제 버튼 리스너 메서드
    fun onClickIconDeleteReview(review: ReviewModel) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                contentsReviewService.deleteContentsReview(review.reviewDocId)
            }
            work1.join()
            // 컨텐츠 별점 수정
            val content = withContext(Dispatchers.IO){
                contentsService.getContentByContentsId(review.contentId)
            }
            val work2 = async(Dispatchers.IO) {
                contentsService.updateContentRatingAndRatingCount(content.contentDocId)
            }
            work2.join()
            getReviewList()
        }
    }


    // 메뉴가 눌릴 때 리스너 메서드
    fun onClickIconMenu(clickPos: Int) {
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
    }

    // 뒤로가기
    fun onClickNavIconBack() {
        tripApplication.navHostController.popBackStack()
    }

    // 며칠전인지 계산하기
    fun calculationDate(date: Timestamp): Int {
        val now = Timestamp.now()
        val diffInMillis = now.seconds * 1000 + now.nanoseconds / 1_000_000 -
                (date.seconds * 1000 + date.nanoseconds / 1_000_000)

        return TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
    }

    // timeStamp -> String 변환
    fun convertToDateMonth(timeStamp: Timestamp): String {
        // Firestore Timestamp를 Date 객체로 변환
        val date = timeStamp.toDate()

        // 한국 시간대 (Asia/Seoul)로 설정
        val dateFormat = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return dateFormat.format(date)
    }

}