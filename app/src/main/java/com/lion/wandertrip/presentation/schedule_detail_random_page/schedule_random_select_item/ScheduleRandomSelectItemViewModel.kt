package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item


import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.model.UnifiedSpotItem
import com.lion.wandertrip.service.ContentsService
import com.lion.wandertrip.service.TripLocationBasedItemService
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.ContentTypeId
import com.lion.wandertrip.util.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleRandomSelectItemViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripScheduleService: TripScheduleService,
    val userService: UserService,
    val contentsService: ContentsService,
    private val tripLocationBasedItemService : TripLocationBasedItemService,
    private val savedStateHandle: SavedStateHandle

) : ViewModel() {

    val application = context as TripApplication
    // 아이템 타입 관광,숙소,식당
    val itemCode = savedStateHandle["itemCode"] ?: 0
    // 일정 문서 ID
    val tripScheduleDocId: String = savedStateHandle["tripScheduleDocId"] ?: ""
    // 여행지 추가할 날짜
    var scheduleDate = savedStateHandle["scheduleDate"] ?: 0L
    // 위도
    val lat = savedStateHandle["lat"] ?: ""

    // 경도
    val lng = savedStateHandle["lng"] ?: ""

    // 🔽 로딩 상태 추가
    val isLoading = mutableStateOf(false)

 /*   // 일정 리뷰 관련 리스트
    val contentsList = mutableStateListOf<ContentsModel>()*/

    val title :MutableState<String> = mutableStateOf(getTitle())

    fun getTitle():String{
        when(itemCode){
            ContentTypeId.TOURIST_ATTRACTION.contentTypeCode -> return "관광지 추가하기"
            ContentTypeId.RESTAURANT.contentTypeCode -> return "음식점 추가하기"
            ContentTypeId.ACCOMMODATION.contentTypeCode -> return "숙소 추가하기"
        }
        return ""
    }


    // 이전 화면 으로 이동 (일정 상세 화면)
    fun backScreen() {
        application.navHostController.popBackStack()
    }

    // 유저 관심 지역 옵저브
    fun observeUserLikeList() {

    }

/*    // 리뷰 데이터 컬렉션 옵저브
    fun observeContentsData() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("ContentsData")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("observeContentsData", "데이터 옵저브 에러: ${error.message}")
                    return@addSnapshotListener
                }
                querySnapshot?.let { snapshot ->
                    val resultContentsList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ContentsModel::class.java)
                    }
                    // 기존 리스트를 클리어하고 최신 데이터로 업데이트
                    contentsList.clear()
                    contentsList.addAll(resultContentsList)
                    Log.d("observeContentsData", "총 ${contentsList.size}개의 문서를 가져왔습니다.")
                }
            }
    }*/

    // 디테일페이지로 이동
    fun moveToDetailScreen(contentId: String) {
       application.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL.name}/$contentId")
    }

    // 일정에 여행지 항목 추가
    fun addTripItemToSchedule(tripItemModel: TripLocationBasedItem) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                val scheduleItem = ScheduleItem(
                    itemTitle = tripItemModel.title?:"타이틀",
                    itemType = when (tripItemModel.contentTypeId) {
                        ContentTypeId.TOURIST_ATTRACTION.contentTypeCode.toString() -> "관광지"
                        ContentTypeId.RESTAURANT.contentTypeCode.toString() -> "음식점"
                        ContentTypeId.ACCOMMODATION.contentTypeCode.toString() -> "숙소"
                        else -> ""
                    },
                    itemDate = Timestamp(scheduleDate,0),
                    itemLatitude = tripItemModel.mapLat!!.toDouble(),
                    itemLongitude = tripItemModel.mapLng!!.toDouble(),
                    itemContentId = tripItemModel.contentId!!,
                )

                tripScheduleService.addTripItemToSchedule(
                    tripScheduleDocId,
                    Timestamp(scheduleDate,0),
                    scheduleItem
                )
            }.await()
            application.navHostController.popBackStack()
        }
    }

    // 룰렛 화면으로 이동
    fun moveToRouletteItemScreen(tripScheduleDocId: String, lat: String, lng: Int) {

    }

    // StateFlow 타입의 <contentId , Model>Map 변수
    private val _allContentsMapFlow: StateFlow<Map<String, ContentsModel>> =
        contentsService.getAllContentsModelsFlow()
            .map { list -> list.associate { it.contentId to it } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    val allContentsMapFlow : StateFlow<Map<String,ContentsModel>> = _allContentsMapFlow
    // 좋아요 목록 상태 저장 변수
    // screen 에서 AsCollect로 구독
    private val _userLikeList: StateFlow<List<String>> =
        userService.getUserLikeListFlow(application.loginUserModel.userDocId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userLikeList: StateFlow<List<String>> = _userLikeList

    // 관심 지역 추가, 관심 지역 카운트 증가
    fun addLikeItem(likeItemContentId: String) {
        viewModelScope.launch {
            // 유저 관식목록에 추가
            val work1 = async(Dispatchers.IO) {
                userService.addLikeItem(application.loginUserModel.userDocId, likeItemContentId)
            }
            // 관심 카운트 증가
            val work2 = async(Dispatchers.IO) {
                userService.addLikeCnt(likeItemContentId)
            }
        }
    }

    // 관심 지역 삭제, 관심 지역 카운트 감소
    fun removeLikeItem(likeItemContentId: String) {
        viewModelScope.launch {
            // 유저 관식목록에 제거

            val work1 = async(Dispatchers.IO) {
                userService.removeLikeItem(
                    application.loginUserModel.userDocId,
                    likeItemContentId
                )
            }
            work1.join()
            // 관심 카운트 감소

            val work2 = async(Dispatchers.IO) {
                userService.removeLikeCnt(likeItemContentId)
            }
            work2.join()
        }
    }

    // 좋아요 버튼 누를 때 리스너
    fun toggleFavorite(contentId: String) {
        viewModelScope.launch {
            val userLikeList = _userLikeList
            val isFav = userLikeList.value.contains(contentId)
            // t  상태일때 누르면 f로 전환
            if (isFav) {
                removeLikeItem(contentId)
            } else {
                // f  상태일때 누르면 t로 전환
                addLikeItem(contentId)
            }
        }
    }

    // flow 타입의 각각 리스트 변수
    val itemList = MutableStateFlow<List<UnifiedSpotItem>>(emptyList())
    // 공공데이터와 DB에 있는 컨텐츠를 flow로 만드는 메서드
    private fun createUnifiedSpotItemListFlow(
        contentTypeId: String,
        pageFlow: MutableStateFlow<Int>,
        currentList: MutableStateFlow<List<UnifiedSpotItem>>
    ) {
        val requestedPages = mutableSetOf<Int>()

        viewModelScope.launch {
            // allContentsMapFlow 구독 : 컨텐츠가 바뀔 때마다 리스트에 반영
            _allContentsMapFlow.collect { updatedMap ->
                // currentList 아이템들을 updatedMap에 맞게 privateData 갱신
                currentList.value = currentList.value.map { item ->
                    val updatedPrivateData = item.publicData.contentId?.let { updatedMap[it] }
                    if (updatedPrivateData != item.privateData) {
                        item.copy(privateData = updatedPrivateData)
                    } else {
                        item
                    }
                }
            }
        }

        viewModelScope.launch {
            // 페이지 플로우 구독 : 페이지 바뀔 때마다 새 데이터 요청
            pageFlow.collect { page ->
                if (page in requestedPages) return@collect
                requestedPages.add(page)

                if (lat.isBlank() || lng.isBlank() ) return@collect

                // 공공 데이터 요청
                val (publicItemList, _) = tripLocationBasedItemService.gettingTripLocationBasedItemList(
                    lat = lat,
                    lng = lng,
                    contentTypeId = contentTypeId,
                    page = page,
                    radius = "8",
                    numOfRows = 1000
                )

                // 새로운 UnifiedSpotItem 생성 (최신 맵은 allContentsMapFlow에서 갱신해주므로 여기서는 privateData null 가능)
                val newItems = publicItemList.map { publicItem ->
                    UnifiedSpotItem(
                        publicData = publicItem,
                        privateData = null // 일단 null, 나중에 allContentsMapFlow 콜렉션이 갱신해줌
                    )
                }

                if (newItems.isNotEmpty()) {
                    currentList.value = currentList.value + newItems
                }
            }
        }
    }

    //  페이지 변수
    private val _page = MutableStateFlow(1)


    // 페이지 넘겨 리스트에 받아오기
    fun nextAttraction() {
        Log.d("nextAttraction", "nextAttraction()")
        _page.value++
    }


    init {
        createUnifiedSpotItemListFlow(contentTypeId = itemCode.toString(), currentList = itemList, pageFlow = _page)
    }


}