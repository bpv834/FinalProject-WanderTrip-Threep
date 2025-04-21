package com.lion.wandertrip.presentation.schedule_item_review

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.util.Tools
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ScheduleItemReviewViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val tripScheduleService: TripScheduleService
) : ViewModel() {

    val application = context as TripApplication

    // 기존 DB에서 불러온 ScheduleItem
    val scheduleItem = mutableStateOf(ScheduleItem())

    // 새로 추가하는 사진들의 Bitmap 리스트
    // (이 리스트는 아직 업로드 전 상태)
    val newBitmaps = mutableStateListOf<Bitmap>()

    // 로딩 상태 표시 (로딩 표시)
    val isLoading = mutableStateOf(false)


    // 일정 항목 문서 id로 일정 항목 가져 오기
    fun getScheduleItemByDocId(tripScheduleDocId: String, scheduleItemDocId: String) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.getScheduleItemByDocId(tripScheduleDocId, scheduleItemDocId)
            }
            scheduleItem.value = work1.await() ?: ScheduleItem()
        }
    }

    // 앨범에서 사진을 선택했을 때 호출
    fun onImagePicked(context: Context, uri: Uri) {
        val tempList = mutableListOf<Bitmap?>()
        // 1) Tools.takeAlbumDataList 로 Bitmap 변환 & 리사이즈
        Tools.takeAlbumDataList(context, uri, tempList)
        // tempList에는 변환된 Bitmap이 들어옴

        // null 아닌 것만 newBitmaps에 추가
        // newBitmaps 에 추가 (즉시 업로드 X)
        tempList.forEach { bmp ->
            if (bmp != null) {
                newBitmaps.add(bmp)
            }
        }
    }

    // 사진 삭제 로직
    // 기존 이미지(itemReviewImagesURL) 삭제
    fun removeImageFromOld(index: Int) {
        val oldList = scheduleItem.value.itemReviewImagesURL.toMutableList()
        if (index in oldList.indices) {
            oldList.removeAt(index)
            scheduleItem.value = scheduleItem.value.copy(itemReviewImagesURL = oldList).toScheduleItemModel()
        }
    }

    // 아직 업로드 전인 newBitmaps에서 삭제
    fun removeImageFromNew(index: Int) {
        if (index in newBitmaps.indices) {
            newBitmaps.removeAt(index)
        }
    }

    // 추가한 이미지 Storage 업로드 후 DB에 저장
    fun saveReview(tripScheduleDocId: String, scheduleItemDocId: String, reviewText: String) {
        viewModelScope.launch {
            Log.d("ScheduleItemReviewViewModel", "🔹 저장 시작")

            // 🔹 저장 시작 -> 로딩 표시
            isLoading.value = true

            // (1) newBitmaps -> 업로드 -> 다운로드 URL 리스트
            Log.d("ScheduleItemReviewViewModel", "(1) 이미지 업로드 시작")
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.uploadBitmapListToFirebase(newBitmaps)
            }
            val newUrls = work1.await()
            Log.d("ScheduleItemReviewViewModel", "(1) 이미지 업로드 완료: ${newUrls.size}개 업로드됨")

            // (2) 기존 URL + 새 URL 합치기
            Log.d("ScheduleItemReviewViewModel", "(2) 기존 이미지와 새 이미지 합치기")
            val oldList = scheduleItem.value.itemReviewImagesURL
            val finalList = oldList + newUrls
            Log.d("ScheduleItemReviewViewModel", "(2) 최종 이미지 리스트 크기: ${finalList.size}")

            // (3) scheduleItem 업데이트
            Log.d("ScheduleItemReviewViewModel", "(3) scheduleItem 업데이트 생성")
            val updatedItem = scheduleItem.value.copy(
                itemReviewImagesURL = finalList,
                itemReviewText = reviewText,
            ).toScheduleItemModel()

            updatedItem.itemReviewImagesURL.forEach {
                Log.d("ScheduleItemReviewViewModel", "itemReviewImagesURL : $it")
            }

            // (4) DB에 최종 저장
            Log.d("ScheduleItemReviewViewModel", "(4) DB 저장 시작")
            val work2 = async(Dispatchers.IO) {
                tripScheduleService.updateScheduleItem(
                    tripScheduleDocId = tripScheduleDocId,
                    scheduleItemDocId = scheduleItemDocId,
                    updatedItem
                )
            }.await()
            Log.d("ScheduleItemReviewViewModel", "(4) DB 저장 완료")

            backScreen()
            Log.d("ScheduleItemReviewViewModel", "🔹 저장 완료 후 화면 복귀")
        }
    }
    // 이전 화면(일정 상세)으로 이동
    fun backScreen() {
        application.navHostController.popBackStack()
    }

}
