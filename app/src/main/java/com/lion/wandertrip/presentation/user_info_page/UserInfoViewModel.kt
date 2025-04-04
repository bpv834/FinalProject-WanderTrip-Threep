package com.lion.wandertrip.presentation.user_info_page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.service.ContentsReviewService
import com.lion.wandertrip.service.TripNoteService
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.Tools
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val userService: UserService,
    val tripScheduleService: TripScheduleService,
    val tripNoteService: TripNoteService,
    val contentsReviewService: ContentsReviewService
) : ViewModel() {
    val tripApplication = context as TripApplication

    // 변경할 닉네임 변수
    val textFieldUserNicknameValue = mutableStateOf(tripApplication.loginUserModel.userNickName)

    // 카메라나 앨범을 통해 가져온 사진을 담을 상태변수
    val imageBitmapState = mutableStateOf<Bitmap?>(null)

    // 보여줄 이미지의 Uri
    val showImageUri = mutableStateOf<Uri?>(null)

    // 앨범이나 사진에서 이미지를 받아왔는지 상태 관리 변수
    val isImagePicked = mutableStateOf(false)

    // 로딩 상태 변수
    val isLoading = mutableStateOf(false)

    // 네비게이션 아이콘을 누르면 호출되는 메서드
    fun onClickNavIconBack() {
        tripApplication.navHostController.popBackStack()
    }

    // 체크 아이콘 누를 때 리스너
    // 유저 정보 수정 메서드
    fun onClickIconCheck() {
        CoroutineScope(Dispatchers.Main).launch {
            // 로딩상태 변경
            isLoading.value=true
            val userModel = tripApplication.loginUserModel
            var imageName = userModel.userProfileImageURL
            val oldNickName = userModel.userNickName
            var newNickName = ""


            if (isImagePicked.value) {
                // 새로운 이미지 파일명 생성
                imageName = "image_${System.currentTimeMillis()}.jpg"

                // 이미지 저장 (파일 경로 설정)
                Tools.saveBitmap(tripApplication, imageBitmapState.value!!)
            }

            // 닉네임 업데이트
            userModel.userNickName = textFieldUserNicknameValue.value
            newNickName = textFieldUserNicknameValue.value

            userModel.userProfileImageURL = imageName

            // 병렬로 Firestore에 이미지와 사용자 데이터 업데이트
            val filePath = tripApplication.getExternalFilesDir(null).toString()

            val work1 = async(Dispatchers.IO) {

                // fireStore 에 이미지 저장
                if (isImagePicked.value) {
                    userService.uploadImage("$filePath/uploadTemp.jpg", imageName)
                }
            }

            val work2 = async(Dispatchers.IO) {
                // 유저 정보 수정
                userService.updateUserData(userModel)
            }

            // 닉네임 변경을 했다면
            if(oldNickName!= newNickName){
                // 쓴 여행기 닉네임을 변경한 닉네임으로 수정
                val work3= async(Dispatchers.IO){
                    tripNoteService.changeTripNoteNickname(oldNickName,newNickName)
                }
                // 쓴 일정 닉네임을 변경한 닉네임으로 수정
                val work4= async(Dispatchers.IO){
                    tripScheduleService.changeTripScheduleNickName(oldNickName,newNickName)
                }
                // 쓴 리뷰 닉네임을 변경한 닉네임으로 수정
                val work5 = async(Dispatchers.IO){
                    contentsReviewService.changeReviewNickName(oldNickName,newNickName)
                }
                work3.join()
                work4.join()
                work5.join()
            }


            // 모든 작업 완료 후 업데이트
            work1.await()
            work2.await()
            isLoading.value=false

            // 업데이트된 데이터 저장
            tripApplication.loginUserModel = userModel

            // 뒤로 가기
            tripApplication.navHostController.popBackStack()
        }
    }


    // 프로필 이미지 가져오기
    fun hasImageInApplication(){
        if(tripApplication.loginUserModel.userProfileImageURL!=""){
            viewModelScope.launch {
                val work2 = async(Dispatchers.IO){
                    userService.gettingImage(tripApplication.loginUserModel.userProfileImageURL)
                }
                showImageUri.value = work2.await()
            }
        }
    }

    // 내장 메모리에 있는 로그인 토큰 삭제
    fun clearLoginToken() {
        val pref = tripApplication.getSharedPreferences("LoginToken", Context.MODE_PRIVATE)
        pref.edit {
            clear() // 모든 키 삭제
            apply() // 비동기 적용
        }
        val kakaoPrf = tripApplication.getSharedPreferences("KakaoToken", Context.MODE_PRIVATE)
        kakaoPrf.edit {
            clear() // 모든 키 삭제
            apply() // 비동기 적용
        }
    }

    // 내장 메모리에 있는 최근 본 아이템 목록 삭제
    fun clearAllRecentData() {
        val sharedPreferences = tripApplication.getSharedPreferences("RecentItem", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            clear() // 모든 데이터 삭제
            apply()
        }
    }

    // 백스텍 지우고 로그인 화면으로 이동
    @SuppressLint("RestrictedApi")
    fun logOut() {
        clearLoginToken()
       /* // Preference에 login token이 있는지 확인한다.
        val pref = tripApplication.getSharedPreferences("LoginToken", Context.MODE_PRIVATE)
        val loginToken = pref.getString("token", null)
        Log.d("test100", "token: $loginToken")*/
        clearAllRecentData()
        // Preference에 login token이 있는지 확인한다.
       /* val prefItem = tripApplication.getSharedPreferences("RecentItem", Context.MODE_PRIVATE)
        val item = prefItem.getString("recentItemList", null)
        Log.d("test100", "item: $item")*/
        val backStack = tripApplication.navHostController.currentBackStack
        // 로그인 화면으로 이동한다.
        tripApplication.selectedItem.value=0
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_USER_LOGIN.name){
            Log.d("backStack"," 일반 로그아웃 화면 전환")
            Log.d("backStack"," backStack :${backStack.value}")
            popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) { inclusive = true }
            launchSingleTop = true
        }
    }

    // 백스텍 지우고 로그인 화면으로 이동
    fun KakaoLogOut() {
        clearLoginToken()
         // Preference에 login token이 있는지 확인한다.
         val kakaoPref = tripApplication.getSharedPreferences("KakaoToken", Context.MODE_PRIVATE)
         val kToken = kakaoPref.getString("kToken", null)
         Log.d("test100", "kToken: $kToken")
        clearAllRecentData()
        // Preference에 login token이 있는지 확인한다.
         val prefItem = tripApplication.getSharedPreferences("RecentItem", Context.MODE_PRIVATE)
         val item = prefItem.getString("recentItemList", null)
         Log.d("test100", "item: $item")

        tripApplication.selectedItem.value=0
        // 로그인 화면으로 이동한다.
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_USER_LOGIN.name){
            Log.d("backStack"," 카카오 로그아웃 화면 전환")
            // 이미 로그인 화면이 백스택에 있으면 중복 생성 방지
            popUpTo(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) { inclusive = true }
            launchSingleTop = true
        }

    }


}
