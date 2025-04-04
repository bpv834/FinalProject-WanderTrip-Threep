package com.lion.wandertrip.presentation.bottom.schedule_page

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.presentation.bottom.schedule_page.component.ScheduleIconButton
import com.lion.wandertrip.presentation.bottom.schedule_page.component.ScheduleItemList
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.wanderBlueColor
import kotlinx.coroutines.launch

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
) {

    // 일정 데이터 가져 오는 메소드 호출
    LaunchedEffect(Unit) {
       /* viewModel.observeUserScheduleDocIdList()*/
        viewModel.fetchUserScheduleList()
    }

    // 탭 제목 및 Pager 상태
    val tabTitles = listOf("내 일정", "초대 일정")
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { tabTitles.size }
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "일정화면",
                    fontFamily = NanumSquareRound,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(end = 5.dp)
                        .weight(1f)
                )

                // 일정 추가 화면으로 이동하는 아이콘
                ScheduleIconButton(
                    icon = Icons.Filled.Add,
                    size = 30,
                    iconButtonOnClick = { viewModel.addIconButtonEvent() }
                )
            }

            // 탭 레이아웃
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White, // 전체 배경색 설정 (필요하면 변경 가능)
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = wanderBlueColor // 🔥 선택된 탭의 아래 indicator 색상 변경
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = title) }
                    )
                }
            }

            // HorizontalPager: 스와이프 가능한 페이지 뷰
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> {
                        // 첫 번째 페이지: 내 일정
                        ScheduleItemList(
                            dataList = viewModel.myScheduleList,
                            scheduleType = 0,  // 0: 내 일정 1: 초대 받은 일정
                            viewModel = viewModel,
                            onRowClick = { userSchedule ->
                                viewModel.moveToScheduleDetailScreen(userSchedule)
                            }
                        )
                    }
        /*            1 -> {
                        // 두 번째 페이지: 초대 일정
                        ScheduleItemList(
                            dataList = viewModel.invitedScheduleList,
                            viewModel = viewModel,
                            scheduleType = 1,
                            onRowClick = { invitedScheduleList ->
                                viewModel.moveToScheduleDetailScreen(invitedScheduleList)
                            }
                        )
                    }*/
                }
            }
        }
    }
}
