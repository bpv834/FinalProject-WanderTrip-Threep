package com.lion.wandertrip.presentation.detail_review_write_page

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.model.ReviewModel
import com.lion.wandertrip.service.ContentsReviewService
import com.lion.wandertrip.service.ContentsService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.Tools
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailReviewWriteViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val contentsReviewService: ContentsReviewService,
    val contentsService: ContentsService,
    val userService: UserService,
) : ViewModel() {
    val tripApplication = context as TripApplication

    // 별점 점수 상태 관리 변수
    val ratingScoreValue = mutableStateOf(5.0f)

    // 리뷰 내용 상태 변수
    val reviewContentValue = mutableStateOf("")

    // 이미지 가져 왔는지 상태 여부
    val isImagePicked = mutableStateOf(false)

    // 비트맵 리스트 상태 변수
    val mutableBitMapList = mutableStateListOf<Bitmap?>()

    // 로딩 변수
    val isLoading = mutableStateOf(false)


    // 뒤로가기
    fun onClickNavIconBack() {
        tripApplication.navHostController.popBackStack()
    }

    // 리뷰 작성 메서드
    fun addContentsReview(contentId: String, title: String) {
        Log.d("addContentsReview", "▶ 리뷰 작성 시작 - contentId: $contentId, title: $title")

        viewModelScope.launch {
            val imagePathList = mutableListOf<String>()
            val serverFilePathList = mutableListOf<String>()
            var contentsDocId = ""
            var imageUrlList = listOf<String>()

            if (isImagePicked.value) {
                Log.d("addContentsReview", "✔ 이미지가 선택됨 - 비트맵 수: ${mutableBitMapList.size}")

                mutableBitMapList.forEachIndexed { index, bitmap ->
                    val name = "image_${index}_${System.currentTimeMillis()}.jpg"
                    serverFilePathList.add(name)

                    try {
                        val savedFilePath = Tools.saveBitmaps(tripApplication, bitmap!!, name)
                        imagePathList.add(savedFilePath)
                        Log.d("addContentsReview", "✔ 이미지 저장 완료 - $savedFilePath")
                    } catch (e: Exception) {
                        Log.e("addContentsReview", "❌ 이미지 저장 실패 - index: $index, error: ${e.message}")
                    }
                }
            } else {
                Log.d("addContentsReview", "✖ 이미지 선택 안됨 - 이미지 업로드 스킵")
            }

            if (isImagePicked.value) {
                Log.d("addContentsReview", "▶ 이미지 업로드 시작 - 파일 수: ${imagePathList.size}")
                val work1 = async(Dispatchers.IO) {
                    try {
                        uploadImage(imagePathList, serverFilePathList, contentId)
                    } catch (e: Exception) {
                        Log.e("addContentsReview", "❌ 이미지 업로드 실패: ${e.message}")
                        emptyList<String>()
                    }
                }
                imageUrlList = work1.await()
                Log.d("addContentsReview", "✔ 이미지 업로드 완료 - URL 리스트: $imageUrlList")
            }

            try {
                contentsDocId = contentsService.isContentExists(contentId)
                Log.d("addContentsReview", "✔ 콘텐츠 존재 확인 - DocId: $contentsDocId")
            } catch (e: Exception) {
                Log.e("addContentsReview", "❌ 콘텐츠 존재 확인 실패: ${e.message}")
            }

            val review = ReviewModel().apply {
                reviewTitle = title
                this.contentId = contentId
                reviewContent = reviewContentValue.value
                reviewImageList = imageUrlList
                reviewRatingScore = ratingScoreValue.value
                reviewWriterNickname = tripApplication.loginUserModel.userNickName
                reviewWriterProfileImgURl = try {
                    userService.gettingImage(tripApplication.loginUserModel.userProfileImageURL).toString()
                } catch (e: Exception) {
                    Log.e("addContentsReview", "❌ 프로필 이미지 URL 불러오기 실패: ${e.message}")
                    ""
                }
            }

            try {
                if (contentsDocId.isNotEmpty()) {
                    Log.d("addContentsReview", "✔ 기존 콘텐츠 문서 있음 - 리뷰 추가 중")
                    contentsReviewService.addContentsReview(review)
                } else {
                    Log.d("addContentsReview", "🆕 콘텐츠 문서 없음 - 새 문서 생성 중")
                    val contents = ContentsModel(contentId = contentId)
                    contentsDocId = contentsService.addContents(contents)
                    Log.d("addContentsReview", "✔ 새 콘텐츠 문서 생성 완료 - DocId: $contentsDocId")
                    contentsReviewService.addContentsReview(review)
                }
            } catch (e: Exception) {
                Log.e("addContentsReview", "❌ 리뷰 추가 실패: ${e.message}")
            }

            try {
                val work2 = async(Dispatchers.IO) {
                    addReviewAndUpdateContents(contentsDocId)
                }
                work2.join()
                Log.d("addContentsReview", "✔ 콘텐츠 별점 업데이트 완료")
            } catch (e: Exception) {
                Log.e("addContentsReview", "❌ 별점 업데이트 실패: ${e.message}")
            }

            tripApplication.navHostController.popBackStack()
            isLoading.value = false
            Log.d("addContentsReview", "🏁 리뷰 작성 종료")
        }
    }


    // url 리스트 리턴받는 메서드
    suspend fun uploadImage(
        sourceFilePath: List<String>,
        serverFilePath: List<String>,
        contentId: String
    ): List<String> {
        Log.d("uploadImage", "sourceFilePath: $sourceFilePath")
        Log.d("uploadImage", "serverFilePath: $serverFilePath")
        Log.d("uploadImage", "contentId: $contentId")

        // 📌 동기적으로 업로드 실행 후 결과 반환
        val resultUrlList = contentsReviewService.uploadReviewImageList(
            sourceFilePath,
            serverFilePath.toMutableList(), // `toMutableStateList()` 제거 (필요 없음)
            contentId
        )

        Log.d("uploadImage", "업로드된 이미지 URL 리스트: $resultUrlList")

        return resultUrlList ?: emptyList() // 업로드 실패 시 빈 리스트 반환
    }


    // 컨텐츠 의 별점 필드 수정
    suspend fun addReviewAndUpdateContents(contentDocId:String) {
        contentsService.updateContentRatingAndRatingCount(contentDocId)
    }


}