package com.lion.wandertrip.presentation.detail_review_modify_page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.lion.wandertrip.TripApplication
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
class DetailReviewModifyViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val contentsReviewService: ContentsReviewService,
    val userService: UserService,
    val contentsService: ContentsService

) : ViewModel() {
    val tripApplication = context as TripApplication

    val reviewModelValue = mutableStateOf(ReviewModel())

    val ratingScoreValue = mutableStateOf(0.0f)

    val reviewContentValue = mutableStateOf("")

    val mutableBitMapList = mutableStateListOf<Bitmap?>()

    val isLoading = mutableStateOf(false)

    val isImagePicked = mutableStateOf(false)


    fun onClickNavIconBack() {
        tripApplication.navHostController.popBackStack()
    }

    // 모델 가져오기
    fun getReviewModel(contentReviewDocId: String) {
        Log.d("test100", " contentReviewDocId : $contentReviewDocId")
        isLoading.value = true
        viewModelScope.launch {
            isLoading.value = true
            // 수정할 리뷰 가져오기
            val work1 = async(Dispatchers.IO) {
                contentsReviewService.getContentsReviewByDocId(contentReviewDocId)
            }
            val reviewData = work1.await()
            Log.d("test", "reviewData : ${reviewData.reviewTitle}")
            reviewModelValue.value = reviewData

            val work2 = async(Dispatchers.IO) {
                convertToBitMap()
            }
            work2.join()

            ratingScoreValue.value = reviewData.reviewRatingScore

            reviewContentValue.value = reviewData.reviewContent
            isLoading.value = false

        }
    }

    // 비트맵 객체로 변환
    suspend fun convertToBitMap() {
        Log.d("bitmapLog", "🧹 비트맵 리스트 초기화")
        mutableBitMapList.clear()

        val urlList = reviewModelValue.value.reviewImageList
        Log.d("bitmapLog", "📸 이미지 URL 목록: $urlList")

        val bitMapList = mutableListOf<Bitmap>()

        // 각 URL에 대해 비트맵을 로드하여 리스트에 추가
        for (url in urlList) {
            try {
                Log.d("bitmapLog", "🌐 비트맵 로드 시작: $url")
                val bitmap = loadImageAsBitmap(url)

                if (bitmap != null) {
                    bitMapList.add(bitmap)
                    Log.d("bitmapLog", "✅ 비트맵 로드 성공: $url")
                } else {
                    Log.w("bitmapLog", "⚠️ 비트맵이 null입니다: $url")
                }
            } catch (e: Exception) {
                Log.e("bitmapLog", "❌ 비트맵 로드 실패: $url, 에러: ${e.message}", e)
            }
        }

        mutableBitMapList.addAll(bitMapList)
        Log.d("bitmapLog", "🖼️ 최종 비트맵 리스트 크기: ${mutableBitMapList.size}")
    }


    // url -> bitmap
    suspend fun loadImageAsBitmap(url: String): Bitmap? {
        Log.d("bitmapLoader", "🚀 이미지 로딩 시작: $url")

        return try {
            val imageLoader = ImageLoader(tripApplication)
            val imageRequest = ImageRequest.Builder(tripApplication)
                .data(url)
                .build()

            // 이미지를 로드하고 결과를 비트맵으로 변환
            val result = imageLoader.execute(imageRequest)
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap

            if (bitmap != null) {
                Log.d("bitmapLoader", "✅ 비트맵 변환 성공: $url")
            } else {
                Log.w("bitmapLoader", "⚠️ drawable이 BitmapDrawable이 아님 or null: $url")
            }

            bitmap
        } catch (e: Exception) {
            Log.e("bitmapLoader", "❌ 이미지 로딩 실패: $url, 에러: ${e.message}", e)
            null
        }
    }


    // 수정 완료하기
    fun onClickIconCheckModifyReview(
        paramContentsId: String,
        reviewDocID: String,
    ) {
        isLoading.value = true
        Log.d("onClickIconCheckModifyReview", "onClickIconCheckModifyReview")
        viewModelScope.launch {
            val imagePathList = mutableListOf<String>()
            val serverFilePathList = mutableListOf<String>()

            val work0 = async(Dispatchers.IO) {
                contentsReviewService.getContentsReviewByDocId(reviewDocID)
            }
            val gettingReview = work0.await()
            var imageUrlList = gettingReview.reviewImageList


            if (isImagePicked.value) {
                Log.d("addContentsReview", "이미지 선택됨, 저장 시작")

                // 외장 메모리에 bitmap 저장
                mutableBitMapList.forEachIndexed { index, bitmap ->
                    val name = "image_${index}_${System.currentTimeMillis()}.jpg"
                    serverFilePathList.add(name)

                    val savedFilePath = Tools.saveBitmaps(tripApplication, bitmap!!, name)
                    Log.d("checkFile", "파일 저장 경로: $savedFilePath")

                    imagePathList.add(savedFilePath)
                }
                Log.d("addContentsReview", "이미지 저장 완료 - 총 ${imagePathList.size}개")
            }

            if (isImagePicked.value) {
                Log.d("addContentsReview", "이미지 업로드 시작")
                val work1 = async(Dispatchers.IO) {
                    uploadImage(imagePathList, serverFilePathList, gettingReview.contentId)
                }
                imageUrlList = work1.await()
                Log.d("getUri", "이미지 업로드 완료 - URL 리스트: $imageUrlList")
            } else {
                Log.d("addContentsReview", "이미지 선택 안 됨, 업로드 스킵")
            }

            // 📌 업로드가 끝난 후 리뷰 데이터 저장
            Log.d("addContentsReview", "리뷰 데이터 생성 시작")

            val review = ReviewModel().apply {
                reviewTitle = gettingReview.reviewTitle
                reviewDocId = reviewDocID
                this.contentId = paramContentsId
                reviewContent = reviewContentValue.value
                reviewImageList = imageUrlList // ✅ 업로드 완료 후 URL 리스트 저장
                reviewRatingScore = ratingScoreValue.value
                reviewWriterNickname = tripApplication.loginUserModel.userNickName
                reviewWriterProfileImgURl =
                    userService.gettingImage(tripApplication.loginUserModel.userProfileImageURL)
                        .toString()
            }


            Log.d("addContentsReview", "리뷰 데이터 생성 완료: $review")

            val work2 = async(Dispatchers.IO) {
                contentsReviewService.modifyContentsReview( review)
            }
            work2.join()
            Log.d("addContentsReview", "리뷰 수정 완료")

            Log.d("addContentsReview", "리뷰 수정 후 컨텐츠 업데이트 시작")
            val work3 = async(Dispatchers.IO) {
                addReviewAndUpdateContents(paramContentsId)
            }
            work3.join()
            Log.d("addContentsReview", "리뷰 저장 후 컨텐츠 업데이트 완료")

            Log.d("addContentsReview", "화면 뒤로 이동")
            isLoading.value = false
            tripApplication.navHostController.popBackStack()
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
    suspend fun addReviewAndUpdateContents(contentId: String) {
        val content = contentsService.getContentByContentsId(contentId)
        contentsService.updateContentRatingAndRatingCount(content.contentDocId)
    }


}