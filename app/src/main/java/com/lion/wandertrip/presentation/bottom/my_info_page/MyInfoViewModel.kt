package com.lion.wandertrip.presentation.bottom.my_info_page

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.RecentTripItemModel
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.ScheduleScreenName
import com.lion.wandertrip.util.Tools
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
class MyInfoViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val userService: UserService,
    val tripScheduleService: TripScheduleService,
) : ViewModel() {

    // userModelState
    val userModelValue = mutableStateOf(UserModel())

    // context
    val tripApplication = context as TripApplication

    // 보여줄 이미지의 Uri
    val showImageUri = mutableStateOf<Uri?>(null)

    // 최근 본 아이템 목록
    val recentTripItemList = mutableStateListOf<RecentTripItemModel>()

    // 프로필 편집
    fun onClickTextUserInfoModify() {
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_USER_INFO.name)
    }

    // 내 여행 화면 전환
    fun onClickIconMyTrip() {
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_MY_TRIP.name)
    }

    // 내 저장 화면 전환
    fun onClickIconMyInteresting() {
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_MY_INTERESTING.name)
    }

    // 내 리뷰 화면 전환
    fun onClickIconMyReview() {
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_MY_REVIEW.name)
    }

    // 내 여행기 화면 전환
    fun onClickIconTripNote() {
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_MY_TRIP_NOTE.name)
    }

    // 최근 게시물 클릭 리스너
    fun onClickCardRecentContent(contentId: String) {
        tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_DETAIL.name}/$contentId")
    }

    // 내 여행 상세로 화면 전환 메서드
    fun onClickScheduleItemGoScheduleDetail(tripSchedule: TripScheduleModel) {
        tripApplication.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_DETAIL_RANDOM_SCREEN.name}?" +
                    "tripScheduleDocId=${tripSchedule.tripScheduleDocId}&lat=${tripSchedule.lat}&lng=${tripSchedule.lng}",
        ) {
            popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) { inclusive = false }
            // 예를 들어 버튼 연타할 때 같은 화면이 여러 번 쌓이는 것 방지
            launchSingleTop = true // 새 인스턴스를 만들지 않고, 기존 인스턴스를 재사용함
        }
    }


    // userModel 가져오기
    fun gettingUserModel() {
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                userService.getUserByUserDocId(tripApplication.loginUserModel.userDocId)
            }
            userModelValue.value = work1.await()
            if (userModelValue.value.userProfileImageURL != "") {
                val work2 = async(Dispatchers.IO) {
                    userService.gettingImage(userModelValue.value.userProfileImageURL)
                }
                showImageUri.value = work2.await()
            }
        }
    }

    // 화면 열 때 최근 본 목록 가져오기
    fun getRecentTripItemList() {
        recentTripItemList.clear()
        val recentList = Tools.getRecentItemList(tripApplication)
        recentTripItemList.addAll(
            recentList
        )
    }

    // flow 유저 스케줄 상태 변수
    private val _userSchedules = MutableStateFlow<List<TripScheduleModel>>(emptyList())
    val userSchedules: StateFlow<List<TripScheduleModel>> = _userSchedules

    // flow 유저 스케줄 collect 메서드
    private fun getMySchedule() {
        viewModelScope.launch {
            _userSchedules.value =
                tripScheduleService.gettingMyTripSchedules(tripApplication.loginUserModel.userNickName)

        }
    }

    init {
        // 플로우만 넣어야 하는게
        // mutableState 같은 상태는 화면일 리컴퍼지션 될때마다 get해줘야 상태 변경이 됨
        gettingUserModel()
        getMySchedule()
    }


}