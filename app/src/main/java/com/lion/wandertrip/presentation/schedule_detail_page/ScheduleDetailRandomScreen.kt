package com.lion.wandertrip.presentation.schedule_detail_page


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.schedule_detail_page.components.ScheduleDetailRandomDateList
import com.lion.wandertrip.ui.theme.NanumSquareRound

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailRandomScreen(
    viewModel: ScheduleDetailRandomViewModel = hiltViewModel(),
) {
    val isFirstLaunch = rememberSaveable { mutableStateOf(true) } // ✅ 처음 실행 여부 저장
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {

    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    ),
                    title = {
                        Text(
                            text = viewModel.tripSchedule.value.scheduleTitle,
                            fontFamily = NanumSquareRound
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.backScreen() }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "뒤로 가기")
                        }
                    },
                    actions = {
                        //Todo 친구초대 기능 재구현해야함
            /*            IconButton(onClick = {
                            viewModel.moveToScheduleDetailFriendsScreen()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.People,
                                contentDescription = "함께 하는 사람 목록"
                            )
                        }*/
                    },
                )
            }
        ) {
            Column(modifier = Modifier.padding(it)) {
                ScheduleDetailRandomDateList(
                    viewModel = viewModel,
                    tripSchedule = viewModel.tripSchedule.value,
                    formatTimestampToDate = { timestamp -> viewModel.formatTimestampToDate(timestamp) }
                )
            }
        }

        // ✅ 로딩 화면 추가 (투명 오버레이)
        if (isLoading) {
            LottieLoadingIndicator() // ✅ 로딩 애니메이션
        }
    }
}