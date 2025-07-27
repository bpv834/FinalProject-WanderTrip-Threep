package com.lion.wandertrip.presentation.bottom.trip_note_page

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.TripNoteScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.internal.toImmutableList
import javax.inject.Inject

@HiltViewModel
class TripNoteViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripNoteService: TripNoteService,
) : ViewModel() {

    val tripApplication = context as TripApplication

    // TopAppBar의 타이틀
    val topAppBarTitle = mutableStateOf("여행기 모아보기")

    // 글 목록을 구성하기 위한 상태 관리 변수
    var tripNoteList = mutableStateListOf<TripNoteModel>()

    // lottie 상태변수
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading


    // + 버튼(fab 버튼)을 눌렀을 때
    fun addButtonOnClick(){
        val docId = ""
        // 여행기 추가하는 화면으로 이동
        // 수정 (✅ 쿼리 파라미터 - 정상 동작)
        tripApplication.navHostController.navigate("${TripNoteScreenName.TRIP_NOTE_WRITE.name}?scheduleDocId=$docId")
    }

    // 각 항목을 눌렀을 때
    fun listItemOnClick(documentId : String){
        // 여행기 상세보기 화면으로 이동 (각 항목의 문서 id를 전달... 추후에)
        tripApplication.navHostController.navigate("${TripNoteScreenName.TRIP_NOTE_DETAIL.name}/${documentId}")
    }

    // 여행기 가져오는 메서드
    fun gettingTripNoteData() {
        viewModelScope.launch {
            _isLoading.value = true // ✅ 로딩 시작

            try {
                val work1 = async(Dispatchers.IO) {
                    tripNoteService.gettingTripNoteList()
                }

                val recyclerViewList = work1.await().mapIndexed { index, tripNoteModel ->
                    index to tripNoteModel
                }

                tripNoteList.clear()
                tripNoteList.addAll(recyclerViewList.map { it.second })

            } catch (e: Exception) {
                Log.e("TripNoteError", "Error loading trip notes", e)
            } finally {
                _isLoading.value = false // ✅ 로딩 끝
            }
        }
    }

}