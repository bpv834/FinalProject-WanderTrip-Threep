package com.lion.wandertrip.presentation.bottom.home_page

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.service.ContentsService
import com.lion.wandertrip.service.TripAreaBaseItemService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.TripNoteScreenName
import com.lion.wandertrip.vo.TripNoteVO
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val tripNoteService: TripNoteService,
    val tripAreaBaseItemService: TripAreaBaseItemService,
    val contentsService: ContentsService,
    val userService: UserService
) : ViewModel() {


    val tripApplication = context as TripApplication

    // ✅ 사용자 정보 (LiveData로 관리하여 UI에서 감지할 수 있도록 변경)
    private val _userModel = MutableLiveData<UserModel>()
    val userModel: LiveData<UserModel> get() = _userModel

    private val _topScrapedTrips = MutableLiveData<List<TripNoteModel>>()
    val topScrapedTrips: LiveData<List<TripNoteModel>> get() = _topScrapedTrips

    private val _tripNoteList = MutableLiveData<List<TripNoteModel>>()
    val tripNoteList: LiveData<List<TripNoteModel>> get() = _tripNoteList

    private val _imageUrlMap = mutableStateMapOf<String, String?>()
    val imageUrlMap: Map<String, String?> get() = _imageUrlMap

    private val _randomTourItems = MutableLiveData<List<TripItemModel>>() // ✅ LiveData 추가
    val randomTourItems: LiveData<List<TripItemModel>> get() = _randomTourItems

    private val _isLoading = MutableLiveData(false) // ✅ 로딩 상태 추가
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var isFetched = false // 🔥 데이터가 로드되었는지 여부를 저장

    private val _contentsModelMap = MutableStateFlow<Map<String, ContentsModel>>(emptyMap())
    val contentsModelMap: StateFlow<Map<String, ContentsModel>> get() = _contentsModelMap

    // 사용자 좋아요 맵 상태변수
    private val _favoriteMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // get 변수
    val favoriteMap: StateFlow<Map<String, Boolean>> = _favoriteMap

    fun loadFavorites() {
        Log.d("loadFavorites", "loadFavorites 메서드 실행")
        viewModelScope.launch {
            // 좋아요 목록 컨텐츠 아이디 리스트 가져오기
            val result =
                userService.gettingUserLikeList(tripApplication.loginUserModel.userDocId) // 서버 통신
            // associateBy -> key, value 로 맵을 생성
            _favoriteMap.value = result.associateBy({ it }, { true })
            result.forEach { Log.d("loadFavorites", "contentId: ${it}") }
        }
    }

    // 좋아요 버튼 누를 때 리스너
    fun toggleFavorite(contentId: String) {
        viewModelScope.launch {
            val current = _favoriteMap.value.toMutableMap()
            val isFav = current[contentId] == true
            // t  상태일때 누르면 f로 전환
            if (isFav) {
                removeLikeItem(contentId)
                current[contentId] = false
            } else {
                // f  상태일때 누르면 t로 전환

                addLikeItem(contentId)
                current[contentId] = true
            }
            _favoriteMap.value = current
        }
    }

    fun fetchContentsModel(contentId: String) {
        Log.d("test100", "fetchContentsModel")
        viewModelScope.launch {
            val contentsData = contentsService.getContentByContentsId(contentId)
            _contentsModelMap.value =
                _contentsModelMap.value.orEmpty() + (contentId to contentsData)
            Log.d("test100", "map : ${_contentsModelMap.value}")
        }
    }

    // 관심 지역 추가, 관심 지역 카운트 증가
    fun addLikeItem(likeItemContentId: String) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                userService.addLikeItem(tripApplication.loginUserModel.userDocId, likeItemContentId)
            }

            val work2 = async(Dispatchers.IO) {
                userService.addLikeCnt(likeItemContentId)
            }
        }
    }

    // 관심 지역 삭제, 관심 지역 카운트 감소
    fun removeLikeItem(likeItemContentId: String) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                userService.removeLikeItem(
                    tripApplication.loginUserModel.userDocId,
                    likeItemContentId
                )
            }
            work1.join()

            val work2 = async(Dispatchers.IO) {
                userService.removeLikeCnt(likeItemContentId)
            }
            work2.join()
        }
    }

    // 여행기 가져오기
    fun fetchTripNotes() {
        Log.d("test100", "fetchTripNotes")
        viewModelScope.launch {

        }
    }

    // 상위 여행기 가져오기
    fun getTopScrapedTrips() {
        viewModelScope.launch {
            val tripNotes = tripNoteService.gettingTripNoteListWithScrapCount()
            val top7List = tripNotes.sortedByDescending { it.tripNoteScrapCount }
                .take(7) // ✅ 스크랩 수 기준 상위 3개 추출
            _topScrapedTrips.value = top7List
        }
    }

    fun backScreen() {
        tripApplication.navHostController.popBackStack()
    }

    // 내 리뷰 화면 전환
    fun onClickIconSearch() {
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_SEARCH.name)
    }

    fun onClickTrip(contentId: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL.name}/$contentId")
    }

    fun onClickTripNote(documentId: String) {
        tripApplication.navHostController.navigate("${TripNoteScreenName.TRIP_NOTE_DETAIL.name}/${documentId}")
    }

    // 인기도시 리스너
    fun onClickPopularCity(lat: Double, lng: Double, cityName: String, radius:String) {
  /*      println("/${lat}/${lng}/${cityName}/${radius}")*/
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_POPULAR_CITY.name}/${lat}/${lng}/${cityName}/${radius}")
    }
}