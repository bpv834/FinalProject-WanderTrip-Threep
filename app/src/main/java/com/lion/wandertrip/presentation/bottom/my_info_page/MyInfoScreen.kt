package com.lion.wandertrip.presentation.bottom.my_info_page

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lion.wandertrip.presentation.bottom.my_info_page.components.HorizontalRecentPostsList
import com.lion.wandertrip.presentation.bottom.my_info_page.components.HorizontalScheduleList
import com.lion.wandertrip.presentation.bottom.my_info_page.components.ProfileCardBasicImage
import com.lion.wandertrip.util.CustomFont
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("RestrictedApi")
@Composable
fun MyInfoScreen(myInfoViewModel: MyInfoViewModel = hiltViewModel()) {
    Log.d("myScreen", "마이페이지")

    val navController = myInfoViewModel.tripApplication.navHostController
    var backStackRoutes by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        myInfoViewModel.getRecentTripItemList()
    }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collectLatest { backStackEntry ->
            // 현재 백스택을 안전하게 가져옴
            val backStackList =
                navController.currentBackStack.value.mapNotNull { it.destination.route }

            backStackRoutes = backStackList // 최신 백스택 반영
        }
    }

    // 백스택 로그 출력
    LaunchedEffect(backStackRoutes) {
        Log.d("BackStack", "===== Current BackStack =====")
        backStackRoutes.forEach { route ->
            Log.d("BackStack", "Route: $route")
        }
        Log.d("BackStack", "=============================")
    }


    val userModel = myInfoViewModel.userModelValue.value
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,  // 콘텐츠를 상단에 배치
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileCardBasicImage(
                userNickName = userModel.userNickName,
                viewModel = myInfoViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))  // 프로필 카드와 일정 리스트 사이 간격

            // 일정 리스트
            HorizontalScheduleList(myInfoViewModel)

            Column {
                Text("최근 본 항목", fontFamily = CustomFont.customFontBold)
                // 최근 게시글 리스트
                HorizontalRecentPostsList(
                    myInfoViewModel.recentTripItemList,
                    myInfoViewModel
                )
            }


        }
    }
}