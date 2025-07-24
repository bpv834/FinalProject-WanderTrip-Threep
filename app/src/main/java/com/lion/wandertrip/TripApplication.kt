package com.lion.wandertrip

import android.app.Application
import android.content.res.Resources
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.kakao.sdk.common.KakaoSdk
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.model.PopularCityModel
import dagger.hilt.android.HiltAndroidApp

// Hilt를 사용하려면 이 어노테이션을 애플리케이션의 Application 클래스에 선언해야 합니다.
@HiltAndroidApp
class TripApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        firebaseFunctions = Firebase.functions("asia-northeast3")

        // Firebase Auth 익명 로그인
        Firebase.auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                 //   Log.d("MainActivity FirebaseAuth", "로그인 성공: ${task.result?.user?.uid}")
                } else {
                  //  Log.e("MainActivity FirebaseAuth ", "로그인 실패", task.exception)
                }
            }


        FirebaseApp.initializeApp(this) // 이게 없으면 Firebase.auth 접근 불가

        // 카카오 SDK 초기화
        KakaoSdk.init(this, "50cccb7489355d937a3b7ca086b508c3")

        // 화면 크기 초기화
        initScreenSize()

    }

    // FirebaseFunctions 인스턴스를 여기에 선언합니다.
    // lateinit var 로 선언하여 나중에 초기화할 것임을 명시합니다.
    lateinit var firebaseFunctions: FirebaseFunctions

    // 로그인한 사용자 객체
    lateinit var loginUserModel: UserModel

    // 네비게이션
    lateinit var navHostController: NavHostController

    var selectedItem =  mutableStateOf(0)

    var screenWidth = 0
    var screenHeight = 0
    var screenRatio = 0f

    private fun initScreenSize() {
        val metrics = Resources.getSystem().displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenRatio = screenWidth.toFloat() / screenHeight.toFloat()
    }



    // 인기 관광지
    val popularTripList: MutableList<TripItemModel> = mutableListOf()

    // 인기 지역
    val popularCities : MutableList<PopularCityModel> = mutableListOf()
}