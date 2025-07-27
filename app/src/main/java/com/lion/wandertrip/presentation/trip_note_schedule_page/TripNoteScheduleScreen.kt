package com.lion.wandertrip.presentation.trip_note_schedule_page

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.presentation.bottom.schedule_page.component.ScheduleItemList
import com.lion.wandertrip.presentation.trip_note_schedule_page.component.TripNoteScheduleItemList
import com.lion.wandertrip.ui.theme.NanumSquareRound

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripNoteScheduleScreen(
    tripNoteScheduleViewModel: TripNoteScheduleViewModel = hiltViewModel(),
) {
    val tripNoteScheduleList by tripNoteScheduleViewModel.scheduleList.collectAsState()
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "일정 리스트",
                        fontFamily = NanumSquareRound,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { tripNoteScheduleViewModel.navigationButtonClick() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White, // 배경색을 흰색으로 설정
                    titleContentColor = Color.Black, // 제목 텍스트 색상 설정 (필요 시 변경)
                    navigationIconContentColor = Color.Black // 네비게이션 아이콘 색상 설정 (필요 시 변경)
                )
            )
        },

        content = { paddingValues ->
            // 일정 목록 표시
            TripNoteScheduleItemList(
                dataList = tripNoteScheduleList,
                viewModel = tripNoteScheduleViewModel,
                onRowClick = { tripSchedule ->
                    // 클릭 시, 다시 이 화면으로 이동
                    tripNoteScheduleViewModel.gettingSchedule(tripSchedule)
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}
