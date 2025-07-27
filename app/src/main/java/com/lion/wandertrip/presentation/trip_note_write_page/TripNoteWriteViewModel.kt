package com.lion.wandertrip.presentation.trip_note_write_page

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.model.ReviewModel
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.Tools
import com.lion.wandertrip.util.TripNoteScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TripNoteWriteViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripNoteService: TripNoteService,
    val savedStateHandle: SavedStateHandle,
    val scheduleService: TripScheduleService,
) : ViewModel() {
    val tripApplication = context as TripApplication

    val tripNoteTitle = mutableStateOf("")
    // 프로그레스 상태
    val isProgressVisible = mutableStateOf(false)
    // 일정 문서 id
    val scheduleDocId = savedStateHandle.get<String>("scheduleDocId")

    val tripNoteTitleError = mutableStateOf("")

    val tripNoteTitleIsError = mutableStateOf(false)
    // 여행기 내용, 에러
    val tripNoteContent = mutableStateOf("")
    val tripNoteContentError = mutableStateOf("")

    val tripNoteContentIsError = mutableStateOf(false)
    // Bitmap
    val tripNotePreviewBitmap = mutableStateListOf<Bitmap?>()

    // 저장할 이미지 경로 리스트
    val tripNoteImages = mutableStateListOf<String>()

    // 이미지 가져 왔는지 상태 여부
    val isImagePicked = mutableStateOf(false)

    // 선택된 스케줄
    val _pickedSchedule = MutableStateFlow(TripScheduleModel())
    val pickedSchedule : StateFlow<TripScheduleModel> = _pickedSchedule


    // 뒤로 가기 버튼
    fun navigationButtonClick(){
        tripApplication.navHostController.popBackStack()
    }

    // 일정 추가 버튼
    fun addTripScheduleClick() {
        tripApplication.navHostController.navigate(TripNoteScreenName.TRIP_NOTE_SCHEDULE.name) {
            popUpTo(TripNoteScreenName.TRIP_NOTE_SCHEDULE.name) {
                inclusive = false // 해당 화면은 남겨둠
            }
            launchSingleTop = true
        }
    }
    // 사진 삭제
    fun removeTripNoteImage(bitmap: Bitmap) {
        tripNotePreviewBitmap.remove(bitmap)
    }

    // url 리스트 리턴받는 메서드
    suspend fun uploadImage(
        sourceFilePath: List<String>,
        serverFilePath: List<String>,
        noteTitle: String
    ): List<String> {
    /*    Log.d("TripNoteWriteViewModel", "sourceFilePath: $sourceFilePath")
        Log.d("TripNoteWriteViewModel", "serverFilePath: $serverFilePath")
        Log.d("TripNoteWriteViewModel", "contentId: $noteTitle")*/

        // 📌 동기적으로 업로드 실행 후 결과 반환
        val resultUrlList = tripNoteService.uploadTripNoteImageList(
            sourceFilePath,
            serverFilePath.toMutableList(), // `toMutableStateList()` 제거 (필요 없음)
            noteTitle
        )

 //       Log.d("TripNoteWriteViewModel", "업로드된 이미지 URL 리스트: $resultUrlList")

        return resultUrlList ?: emptyList() // 업로드 실패 시 빈 리스트 반환
    }

    // 스케줄 가져오기
    fun gettingSchedule(){
        viewModelScope.launch {
            _pickedSchedule.value=scheduleService.getTripSchedule(scheduleDocId?:"")?:TripScheduleModel()
        }
    }

    // 게시하기 버튼
    fun tripNoteDoneClick(){
        // 저장
        CoroutineScope(Dispatchers.Main).launch {
        isProgressVisible.value = true

        val tripNoteTitle = tripNoteTitle.value
        val tripNoteContent = tripNoteContent.value
        val imagePathList = mutableListOf<String>()
        val serverFilePathList = mutableListOf<String>()
        var imageUrlList = listOf<String>()

        if (isImagePicked.value) {

            // 외장 메모리에 bitmap 저장
            tripNotePreviewBitmap.forEachIndexed { index, bitmap ->
                val name = "image_${index}_${System.currentTimeMillis()}.jpg"
                serverFilePathList.add(name)

                val savedFilePath = Tools.saveBitmaps(tripApplication, bitmap!!, name)

                imagePathList.add(savedFilePath)
            }
        }

        if (isImagePicked.value) {
            Log.d("TripNoteWriteViewModel","isImagePicked == true  사진이 골라짐")
            val work1 = async(Dispatchers.IO) {
                uploadImage(imagePathList, serverFilePathList, tripNoteTitle)
            }
            imageUrlList = work1.await()
        } else {
            Log.d("TripNoteWriteViewModel", "이미지 선택 안 됨, 업로드 스킵")
        }

        //  업로드가 끝난 후 리뷰 데이터 저장
        // 로그인한 사용자의 닉네임
        val userNickname = tripApplication.loginUserModel.userNickName
            // 이미지가 첨부되어 있다면
            for (url in imageUrlList) {
                // 이미지 경로를 리스트에 추가
                tripNoteImages.add(url)
            }

            // 서버에 저장할 여행기 데이터
            val tripNoteModel = TripNoteModel()
            tripNoteModel.tripNoteTitle = tripNoteTitle
            tripNoteModel.tripNoteContent = tripNoteContent
            tripNoteModel.tripNoteImage = tripNoteImages
            tripNoteModel.tripScheduleDocumentId = scheduleDocId?:""
            tripNoteModel.userNickname = userNickname
            tripNoteModel.location = _pickedSchedule.value.scheduleCity
            tripNoteModel.lat = _pickedSchedule.value.lat
            tripNoteModel.lng=_pickedSchedule.value.lng

            try {
                // 저장하기
                val work2 = async(Dispatchers.IO) {
                    tripNoteService.addTripNoteData(tripNoteModel)
                }
                val documentId = work2.await()

                // 프로그레스 바 숨기기
                isProgressVisible.value = false

                // 바텀내브스크린->작성->일정선택->작성->상세
                // 작성포함 지워버리고 상세로 가는 메서드?
                tripApplication.navHostController.navigate("${TripNoteScreenName.TRIP_NOTE_DETAIL.name}/${documentId}") {
                    popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }


            } catch (e: Exception) {
                Log.e("TripNote", "Error saving trip note", e)
                isProgressVisible.value = false
                // 에러 메시지 표시 (옵션)
            }
        }
    }

}