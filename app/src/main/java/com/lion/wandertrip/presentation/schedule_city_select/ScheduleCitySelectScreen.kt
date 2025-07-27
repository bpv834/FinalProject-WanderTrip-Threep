package com.lion.wandertrip.presentation.schedule_city_select

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.lion.wandertrip.component.HomeSearchBar
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.schedule_city_select.component.CityRouletteButton
import com.lion.wandertrip.presentation.schedule_city_select.component.ScheduleCitySelectList

@Composable
fun ScheduleCitySelectScreen(
    scheduleTitle: String,
    scheduleStartDate: Timestamp,
    scheduleEndDate: Timestamp,
    viewModel: ScheduleCitySelectViewModel = hiltViewModel(),
) {
    // 넘겨 받은 데이터 처리
    viewModel.settingFirstData(scheduleTitle, scheduleStartDate, scheduleEndDate)

    // 포커스 관리 객체 생성
    val focusManager = LocalFocusManager.current

    val isLoading by remember { viewModel.isLoading }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterList by viewModel.filterList.collectAsState()


    Scaffold() { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { // 터치 이벤트 감지 바깥쪽 클릭 시 포커스 해제
                        detectTapGestures(
                            onTap = { focusManager.clearFocus() }
                        )
                    },
            ) {
                HomeSearchBar(
                    query = searchQuery,
                    onSearchQueryChanged = { viewModel.updateQuery(it) },
                    onSearchClicked = {},
                    onClearQuery = { viewModel.updateQuery("") },
                    onBackClicked = { viewModel.backScreen() }
                )
                if (searchQuery.isNotBlank()) {
                    ScheduleCitySelectList(
                        dataList = filterList,
                        scheduleTitle = viewModel.scheduleTitle.value,
                        scheduleStartDate = viewModel.scheduleStartDate.value,
                        scheduleEndDate = viewModel.scheduleEndDate.value,
                        onSelectedCity = { areaName ->
                            // 일정 제목 날짜 아이디 저장 후 일정 상세로 넘어 간다
                            viewModel.addTripSchedule(
                                scheduleTitle = scheduleTitle,
                                scheduleStartDate = scheduleStartDate,
                                scheduleEndDate = scheduleEndDate,
                                areaName = areaName
                            )
                        }
                    )
                } else
                    Column {
                        // 한반도 돌리기 버튼1
                        CityRouletteButton(
                            viewModel, scheduleTitle,
                            scheduleStartDate,
                            scheduleEndDate
                        )
                        // 도시 목록
                        ScheduleCitySelectList(
                            dataList = viewModel.initCities,
                            scheduleTitle = viewModel.scheduleTitle.value,
                            scheduleStartDate = viewModel.scheduleStartDate.value,
                            scheduleEndDate = viewModel.scheduleEndDate.value,
                            onSelectedCity = { areaName ->
                                // 일정 제목 날짜 아이디 저장 후 일정 상세로 넘어 간다
                                viewModel.addTripSchedule(
                                    scheduleTitle = scheduleTitle,
                                    scheduleStartDate = scheduleStartDate,
                                    scheduleEndDate = scheduleEndDate,
                                    areaName = areaName
                                )
                            }
                        )
                    }

            }

            // ✅ 로딩 화면 추가 (투명 오버레이)
            if (isLoading) {
                LottieLoadingIndicator() // ✅ 로딩 애니메이션
            }
        }

    }

}