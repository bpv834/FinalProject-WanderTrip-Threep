package com.lion.wandertrip.presentation.popular_city_page

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.model.UnifiedSpotItem
import com.lion.wandertrip.service.ContentsService
import com.lion.wandertrip.service.TripLocationBasedItemService
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.ContentTypeId
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.PopularCityTap
import com.lion.wandertrip.util.TripNoteScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopularCityViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val tripNoteService: TripNoteService,
    val contentsService: ContentsService,
    val tripLocationBasedItemService: TripLocationBasedItemService,
    val userService: UserService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // hilt를 사용해 arg에서 받은 savedStateHandle 에서 사용할 변수 설정
    private var initialLat: String = savedStateHandle.get<String>("lat") ?: ""
    private var initialLng: String = savedStateHandle.get<String>("lng") ?: ""
    private var initialRadius: String = savedStateHandle.get<String>("radius") ?: ""
    private var initialCityName: String = savedStateHandle.get<String>("cityName") ?: ""

    // context
    val tripApplication = context as TripApplication
    // mutableStateList 타입 ->SnapshotStateList 여행기
    private val _tripNoteList = mutableStateListOf<TripNoteModel>()

    val tripNoteList: SnapshotStateList<TripNoteModel> get() = _tripNoteList

    // 여행기 도시 이름으로 가져오기
    fun getTripNoteListByCity() {
        viewModelScope.launch {
            // 지역 여행기 스크랩 내림차 정렬
            _tripNoteList.addAll(
                tripNoteService.gettingTripNoteByCityName(initialCityName)
                    .sortedByDescending { it.tripNoteScrapCount })
        }
        //Log.d("getTripNoteListByCity","${_tripNoteList.joinToString(" ")}")
    }

    // ViewModel 내부에 작성
    // 데이터 개수 저장
    val totalAttractionCount = mutableStateOf(0)
    val totalRestaurantCount = mutableStateOf(0)
    val totalAccommodationCount = mutableStateOf(0)

    // 좋아요 목록 상태 저장 변수
    // screen 에서 AsCollect로 구독
    private val _userLikeList: StateFlow<List<String>> =
        userService.getUserLikeListFlow(tripApplication.loginUserModel.userDocId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userLikeList: StateFlow<List<String>> = _userLikeList

    // 관심 지역 추가, 관심 지역 카운트 증가
    fun addLikeItem(likeItemContentId: String) {
        viewModelScope.launch {
            // 유저 관식목록에 추가
            val work1 = async(Dispatchers.IO) {
                userService.addLikeItem(tripApplication.loginUserModel.userDocId, likeItemContentId)
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
                    tripApplication.loginUserModel.userDocId,
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


    // 뷰페이저 상태 저장 변수
    private val _tapViewFlow =
        MutableStateFlow(PopularCityTap.POPULAR_CITY_TAP_HOME.num) // 0 -> 홈 1->관광지 2-> 식당 3-> 숙소 4-> 여행기
    val tapViewFlow: StateFlow<Int> = _tapViewFlow

    // 뷰페이저 상태 전환 메서드
    fun changeState(idx: Int) {
        _tapViewFlow.value = idx
    }

    // StateFlow 타입의 <contentId , Model>Map 변수
    val allContentsMapFlow: StateFlow<Map<String, ContentsModel>> =
        contentsService.getAllContentsModelsFlow()
            .map { list -> list.associate { it.contentId to it } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())


    // 관광지 타입 변수
    private val ATTRACTION_CONTENT_TYPE_ID =
        ContentTypeId.TOURIST_ATTRACTION.contentTypeCode.toString()

    // 홈에서 사용하는 관광지 페이지 변수
    private val _attractionPageAtHome = MutableStateFlow(1)
    val attractionPageAtHome: StateFlow<Int> = _attractionPageAtHome

    // 홈에서 사용하는 관광지 리스트
    val attractionListAtHome: StateFlow<List<UnifiedSpotItem>> =
        createUnifiedSpotItemListFlowAtHome(ATTRACTION_CONTENT_TYPE_ID, _attractionPageAtHome)

    // 관광지 에서 사용하는 페이지 변수
    private val _attractionPage = MutableStateFlow(1)

    // 식당 에서 사용하는 페이지 변수
    private val _restaurantPage = MutableStateFlow(1)

    //  숙소에서 사용하는 페이지 변수
    private val _accommodationPage = MutableStateFlow(1)

    //  여행기에서 사용하는 페이지 변수
    private val _tripNotePage = MutableStateFlow(1)


    // 관광지 다음페이지
    fun loadNextAttractionPageAtHome() {
        // 최대 4까지 4->1
        _attractionPageAtHome.value =
            if (_attractionPageAtHome.value >= 4) 1 else _attractionPageAtHome.value + 1
    }

    // 관광지 이전페이지
    fun loadPreAttractionPageAtHome() {
        _attractionPageAtHome.value--
    }


    // 식당 컨텐트 타입 변수
    private val RESTAURANT_CONTENT_TYPE_ID = ContentTypeId.RESTAURANT.contentTypeCode.toString()

    // 식당 페이지 변수
    private val _restaurantPageAtHome = MutableStateFlow(1)
    val restaurantPageAtHome: StateFlow<Int> get() = _restaurantPageAtHome

    // 홈에서 사용하는 식당 리스트
    val restaurantListAtHome: StateFlow<List<UnifiedSpotItem>> =
        createUnifiedSpotItemListFlowAtHome(RESTAURANT_CONTENT_TYPE_ID, _restaurantPageAtHome)


    // 식당 다음페이지
    fun loadNextRestaurantPageAtHome() {
        // 최대 4까지 4->1
        _restaurantPageAtHome.value =
            if (_restaurantPageAtHome.value >= 4) 1 else _restaurantPageAtHome.value + 1
    }

    // 식당 이전페이지
    fun loadPreRestaurantPageAtHome() {
        _restaurantPageAtHome.value--
    }

    // 숙소 타입 저장 변수
    private val ACCOMMODATION_CONTENT_TYPE_ID =
        ContentTypeId.ACCOMMODATION.contentTypeCode.toString()

    // 홈에서 사용하는 숙소 페이지 저장 변수
    private val _accommodationPageAtHome = MutableStateFlow(1)
    val accommodationPageAtHome: StateFlow<Int> get() = _accommodationPageAtHome

    // 홈에서 사용하는 숙소 리스트
    val accommodationListAtHome: StateFlow<List<UnifiedSpotItem>> =
        createUnifiedSpotItemListFlowAtHome(ACCOMMODATION_CONTENT_TYPE_ID, _accommodationPageAtHome)


    // 숙소 다음페이지
    fun loadNextAccommodationPageAtHome() {
        // 최대 4까지 4->1
        _accommodationPageAtHome.value =
            if (_accommodationPageAtHome.value >= 4) 1 else _accommodationPageAtHome.value + 1
    }

    // 숙소 이전페이지
    fun loadPreAccommodationPageAtHome() {
        _accommodationPageAtHome.value--
    }

    // 홈에서 공공데이터와 fireStore content 컬렉션을 합쳐 보여주는 메서드
    private fun createUnifiedSpotItemListFlowAtHome(
        contentTypeId: String,
        pageFlow: MutableStateFlow<Int>,
    ): StateFlow<List<UnifiedSpotItem>> {
        return combine(

            pageFlow,
            allContentsMapFlow,

            ) { page, allContentsMap ->
            // 내용이 존재할때
            if (initialLat.isNotBlank() && initialLng.isNotBlank() && initialRadius.isNotBlank()) {
                //Log.d("createUnifiedSpotItemListFlow","$initialLat $initialLng $contentTypeId $page $initialRadius")
                // 공공데이터 리스트와 토탈개수를 가져온다\

                val (publicItemList, totalCount) = tripLocationBasedItemService.gettingTripLocationBasedItemList(
                    lat = initialLat,
                    lng = initialLng,
                    contentTypeId = contentTypeId,
                    page = page,
                    radius = initialRadius,
                    numOfRows = 4
                )
                // 토탈개수 저장
                when (contentTypeId) {
                    ATTRACTION_CONTENT_TYPE_ID -> {
                        totalAttractionCount.value = totalCount
                    }

                    RESTAURANT_CONTENT_TYPE_ID -> {
                        totalRestaurantCount.value = totalCount
                    }

                    ACCOMMODATION_CONTENT_TYPE_ID -> {
                        totalAccommodationCount.value = totalCount
                    }
                }
                /*   Log.d("createUnifiedSpotItemListFlow","${allContentsMap} ")
                   Log.d("createUnifiedSpotItemListFlow","$publicItemList ")*/


                publicItemList.map { publicItem ->
                    UnifiedSpotItem(
                        publicData = publicItem,
                        privateData = publicItem.contentId?.let { allContentsMap[it] }
                    )
                }

            } else {
                emptyList()
            }
        }
            // 새로 구독을 해서 ui를 그린다.
            // StateFlow로 만들어 자동 캐싱 및 구독
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    }

    // flow 타입의 각각 리스트 변수
    val attractionList = MutableStateFlow<List<UnifiedSpotItem>>(emptyList())
    val restaurantList = MutableStateFlow<List<UnifiedSpotItem>>(emptyList())
    val accommodationList = MutableStateFlow<List<UnifiedSpotItem>>(emptyList())

    // 공공데이터와 DB에 있는 컨텐츠를 flow로 만드는 메서드
    private fun createUnifiedSpotItemListFlow(
        contentTypeId: String,
        pageFlow: MutableStateFlow<Int>,
        currentList: MutableStateFlow<List<UnifiedSpotItem>>
    ) {
        val requestedPages = mutableSetOf<Int>()

        viewModelScope.launch {
            // allContentsMapFlow 구독 : 컨텐츠가 바뀔 때마다 리스트에 반영
            allContentsMapFlow.collect { updatedMap ->
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

                if (initialLat.isBlank() || initialLng.isBlank() || initialRadius.isBlank()) return@collect

                // 공공 데이터 요청
                val (publicItemList, _) = tripLocationBasedItemService.gettingTripLocationBasedItemList(
                    lat = initialLat,
                    lng = initialLng,
                    contentTypeId = contentTypeId,
                    page = page,
                    radius = initialRadius,
                    numOfRows = 10
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

    // 관광지 페이지 넘겨 리스트에 받아오기
    fun nextAttraction() {
        Log.d("nextAttraction", "nextAttraction()")
        _attractionPage.value++
    }

    // 식당 페이지 넘겨 리스트에 받아오기
    fun nextRestaurant() {
        Log.d("nextRestaurant", "nextRestaurant()")
        _restaurantPage.value++
    }

    // 숙소 페이지 넘겨 리스트에 받아오기
    fun nextAccommodation() {
        Log.d("nextAccommodation", "nextAccommodation()")
        _accommodationPage.value++
    }


    init {
        // 각각 리스트 가져오기
        createUnifiedSpotItemListFlow(
            contentTypeId = ATTRACTION_CONTENT_TYPE_ID,
            pageFlow = _attractionPage,
            currentList = attractionList
        )

        createUnifiedSpotItemListFlow(
            contentTypeId = RESTAURANT_CONTENT_TYPE_ID,
            pageFlow = _restaurantPage,
            currentList = restaurantList
        )

        createUnifiedSpotItemListFlow(
            contentTypeId = ACCOMMODATION_CONTENT_TYPE_ID,
            pageFlow = _accommodationPage,
            currentList = accommodationList
        )

        // 여행기 가져오기
        getTripNoteListByCity()


    }

    // 관광지 상세 페이지 이동
    fun onClickTrip(contentId: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL.name}/$contentId")
    }

    // 여행기 상세 페이지 이동
    fun onClickTripNote(documentId: String) {
        tripApplication.navHostController.navigate("${TripNoteScreenName.TRIP_NOTE_DETAIL.name}/${documentId}")
    }

    // 뒤로 가기
    fun onClickBackButton() {
        tripApplication.navHostController.popBackStack()
    }

    /*
    // Composable로부터 값을 설정받는 함수 추가 (Double을 String으로 변환)
    fun setInitialLocation(lat: String, lng: String, radius: String) {
        // 이미 값이 설정되지 않았을 경우에만 업데이트하거나, 항상 업데이트하도록 로직 선택
        if (initialLat.isEmpty() || initialLng.isEmpty() || initialRadius.isEmpty()) {
            initialLat = lat.toString()
            initialLng = lng.toString()
            initialRadius = radius
            // 값이 설정되면 데이터 로딩 시작 (예: `createUnifiedSpotItemListFlow` 재실행 또는 초기 로딩 트리거)
            // 필요한 경우 여기서 데이터 로딩 로직을 다시 호출하거나, Flows가 변경을 감지하도록 합니다.
            // 예를 들어, combine flow가 _initialLat 등의 변경을 자동으로 감지하므로 별도의 호출은 필요 없을 수 있습니다.
            Log.d("PopularCityViewModel", "Initial location set: $lat, $lng, $radius")
        }
    }
*/


}