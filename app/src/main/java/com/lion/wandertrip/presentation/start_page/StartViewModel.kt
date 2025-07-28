package com.lion.wandertrip.presentation.start_page

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.RecentTripItemModel
import com.lion.wandertrip.presentation.start_page.used_dummy_data.RecentDummyData
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val userService: UserService,
) : ViewModel() {

    // 로그인 중입니다를 위한 상태 관리 변수
    val showLoginMessageState = mutableStateOf(false)

    // 카카오 로그인 중입니다를 위한 상태 관리 변수
    val showKakaoLoginMessageState = mutableStateOf(false)

    val tripApplication = context as TripApplication

    // 자동 로그인 처리
    fun autoLoginProcess() {

        // 로그인 화면으로 이동
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_USER_LOGIN.name) {
            // 이미 로그인 화면이 백스택에 있을 경우 중복 생성 방지
            launchSingleTop = true
        }

        // Preference에 login token이 있는지 확인한다.
        val pref = tripApplication.getSharedPreferences("LoginToken", Context.MODE_PRIVATE)
        val loginToken = pref.getString("token", null)
        Log.d("test100", "token: $loginToken")

        // Preference에 login token이 있는지 확인한다.
        val kakaoPref = tripApplication.getSharedPreferences("KakaoToken", Context.MODE_PRIVATE)
        val kToken = kakaoPref.getString("kToken", null)

        Log.d("test100", "kToken : $kToken")

        CoroutineScope(Dispatchers.Main).launch {
            if (loginToken != null) {

                showLoginMessageState.value = true

                // 사용자 정보를 가져온다.
                val work1 = async(Dispatchers.IO) {
                    userService.selectUserDataByLoginToken(loginToken)
                }
                val loginUserModel = work1.await()
                // 가져온 사용자 데이터가 있다면
                if (loginUserModel != null) {
                    tripApplication.loginUserModel = loginUserModel

                /*    tripApplication.navHostController.popBackStack(
                        MainScreenName.MAIN_SCREEN_START.name,
                        true
                    )*/
                    // 일반 자동 로그인
                    tripApplication.navHostController.navigate(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) {
                        Log.d("backStack","일반 자동 로그인1")
                        popUpTo(MainScreenName.MAIN_SCREEN_START.name) { inclusive = true }
                        launchSingleTop = true
                    }

                } else {
                    // 일반 자동 로그인 실패
                    // 로그인 화면으로 이동한다.
                    tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_USER_LOGIN.name){
                        Log.d("backStack","일반 자동 로그인2")

                        // 이미 로그인 화면이 백스택에 있으면 중복 생성 방지
                        popUpTo(MainScreenName.MAIN_SCREEN_START.name) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else if (kToken != null) {
                // 카카오 로그인
                showKakaoLoginMessageState.value = true

                // 카카오토큰으로 사용자 정보를 가져온다.
                val work1 = async(Dispatchers.IO) {
                    userService.selectUserDataByKakaoLoginToken(kToken.toLong())
                }
                val loginUserModel = work1.await()
                // 가져온 사용자 데이터가 있다면
                if (loginUserModel != null) {
                    tripApplication.loginUserModel = loginUserModel
                    /*tripApplication.navHostController.popBackStack(
                        MainScreenName.MAIN_SCREEN_START.name,
                        true
                    )
                    // 카카오 로그인으로 홈 화면 이동
                    tripApplication.navHostController.navigate(BotNavScreenName.BOT_NAV_SCREEN_HOME.name){
                        // 이미 로그인 화면이 백스택에 있으면 중복 생성 방지
                        launchSingleTop = true
                    }*/

                    tripApplication.navHostController.navigate(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) {
                        Log.d("backStack","카톡 자동 로그인1")

                        popUpTo(MainScreenName.MAIN_SCREEN_START.name) { inclusive = true }
                        launchSingleTop = true
                    }

                } else {
                    // 카카오 로그인 실패 시 로그인 화면으로 이동
                    // 로그인 화면으로 이동한다.
                    tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_USER_LOGIN.name){
                        Log.d("backStack","카톡 자동 로그인2")
                        // 이미 로그인 화면이 백스택에 있으면 중복 생성 방지
                        popUpTo(MainScreenName.MAIN_SCREEN_START.name) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                // 로그인 화면으로 이동한다.
                tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_USER_LOGIN.name){
                    Log.d("backStack"," 로그인1")
                    // 이미 로그인 화면이 백스택에 있으면 중복 생성 방지
                    popUpTo(MainScreenName.MAIN_SCREEN_START.name) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }


}