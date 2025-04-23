package com.lion.wandertrip

import android.app.Application
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.kakao.sdk.common.KakaoSdk
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.util.PopularCity
import dagger.hilt.android.HiltAndroidApp

// Hilt를 사용하려면 이 어노테이션을 애플리케이션의 Application 클래스에 선언해야 합니다.
@HiltAndroidApp
class TripApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 카카오 SDK 초기화
        KakaoSdk.init(this, "50cccb7489355d937a3b7ca086b508c3")

        // 화면 크기 초기화
        initScreenSize()

//        // ✅ 기본 유저 정보를 미리 설정 (임시값)
//        loginUserModel = UserModel(
//            userDocId = "",
//            userId = "test123",
//            userNickName = "testNickName1"
//        )
    }

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
    val cities : MutableList<PopularCity> = mutableListOf()
}