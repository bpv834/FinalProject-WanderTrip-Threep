package com.lion.wandertrip.presentation.bottom.my_info_page

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.RecentTripItemModel
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.AreaCode
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.ScheduleScreenName
import com.lion.wandertrip.util.Tools
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyInfoViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val userService: UserService,
    val tripScheduleService: TripScheduleService,
) : ViewModel(){

    // userModelState
    val userModel = UserModel()
    val userModelValue = mutableStateOf(userModel)
    // context
    val tripApplication = context as TripApplication

    // 보여줄 이미지의 Uri
    val showImageUri = mutableStateOf<Uri?>(null)

    // 최근 일정 목록
    val upComingScheduleList = mutableStateListOf<TripScheduleModel>()

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
    fun onClickScheduleItemGoScheduleDetail(tripScheduleDocId : String, areaName:String ) {

        // scheduleCity와 일치하는 AreaCode 찾기 (없으면 0 반환)
        val areaCodeValue = AreaCode.entries.firstOrNull { it.areaName == areaName }?.areaCode ?: 0
        Log.d("ScheduleViewModel", "areaCodeValue: $areaCodeValue")


        tripApplication.navHostController.navigate("${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
                "tripScheduleDocId=${tripScheduleDocId}&areaName=${areaName}&areaCode=$areaCodeValue")
    }

    // userModel 가져오기
    fun gettingUserModel() {
        CoroutineScope(Dispatchers.Main).launch {
            val work1= async(Dispatchers.IO){
                userService.getUserByUserDocId(tripApplication.loginUserModel.userDocId)
            }
            userModelValue.value = work1.await()
            if(userModelValue.value.userProfileImageURL !=""){
                val work2 = async(Dispatchers.IO){
                    userService.gettingImage(userModelValue.value.userProfileImageURL)
                }
                showImageUri.value = work2.await()
            }
        }
    }

    // 화면 열 때 여행 일정 리스트 가져오기
    fun getTripScheduleList() {
        upComingScheduleList.clear()
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO){
                tripScheduleService.gettingMyTripSchedules(tripApplication.loginUserModel.userNickName)
            }
            val myScheduleList = work1.await()
            // 다가오는 일정만 가져오기
            val result = myScheduleList.filter { it.scheduleEndDate> Timestamp.now() }
            upComingScheduleList.addAll(
                result
            )
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


}