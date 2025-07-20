package com.lion.wandertrip.presentation.bottom.schedule_page

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.bottom.schedule_page.component.ScheduleIconButton
import com.lion.wandertrip.presentation.bottom.schedule_page.component.ScheduleItemList
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.wanderBlueColor
import kotlinx.coroutines.launch

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
) {
    LaunchedEffect (Unit){
        viewModel.loadMyScheduleOnce()
    }
    val isLoading by viewModel.isLoading.collectAsState()
    // 일정 데이터 가져오기
    val myScheduleList by viewModel.userSchedules.collectAsState()
    if(isLoading){
        LottieLoadingIndicator()
    }
    else{
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 상단 제목 + 추가 버튼
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "내 일정",
                    fontFamily = NanumSquareRound,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .weight(1f)
                )

                // 일정 추가 버튼
                ScheduleIconButton(
                    icon = Icons.Filled.Add,
                    size = 30,
                    iconButtonOnClick = { viewModel.addIconButtonEvent() }
                )
            }

            // LazyColumn으로 일정 목록 출력
            ScheduleItemList(
                dataList = myScheduleList,
                scheduleType = 0,
                viewModel = viewModel,
                onRowClick = { userSchedule ->
                    viewModel.moveToScheduleDetailScreen(userSchedule)
                }
            )
        }
    }
}