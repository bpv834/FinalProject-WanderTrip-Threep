package com.lion.wandertrip.presentation.schedule_city_select

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.R
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.schedule_city_select.component.ScheduleCitySelectList
import com.lion.wandertrip.presentation.schedule_city_select.component.ScheduleCitySelectSearchBar
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.util.RouletteScreenName

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

    // 검색어
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                CustomTopAppBar(
                    title = "지역 선택",
                    navigationIconImage = Icons.Filled.ArrowBack,
                    navigationIconOnClick = { viewModel.backScreen() },
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .pointerInput(Unit) { // 터치 이벤트 감지 바깥쪽 클릭 시 포커스 해제
                        detectTapGestures(
                            onTap = { focusManager.clearFocus() }
                        )
                    },
            ) {
                // 검색 바
                ScheduleCitySelectSearchBar(
                    query = searchQuery,
                    onSearchQueryChanged = {
                        searchQuery = it
                        // 검색어 변경 시 필터링 실행
                        viewModel.updateFilteredCities(it)
                    },
                    onSearchClicked = {
                        Log.d("ScheduleCitySelectScreen","검색 실행: $searchQuery")
                    },
                    onClearQuery = {
                        searchQuery = "" // ✅ 검색어 초기화
                        viewModel.updateFilteredCities("") // X 버튼 클릭 시 전체 도시 목록 복원
                    }
                )
                // 한반도 돌리기 @@@@@
                Button(
                    onClick = {
                        // 도시 룰렛 화면 으로 이동
                        viewModel.moveToRotateMapScreen(scheduleTitle, scheduleStartDate, scheduleEndDate)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, // ✅ 버튼 배경색: 흰색
                        contentColor = Color(0xFF435C8F) // ✅ 버튼 텍스트 색상: 파란색 (변경 가능)
                    ),
                    shape = RectangleShape // ✅ 버튼을 사각형으로 변경
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.roulette_picture), // ✅ drawable 리소스 추가
                        contentDescription = "룰렛 이미지",
                        modifier = Modifier.size(70.dp).padding(end = 16.dp) // ✅ 아이콘 크기 조정 가능
                    )
                    Text(
                        text = "한반도 돌리기",
                        fontFamily = NanumSquareRoundRegular,
                        fontSize = 35.sp,
                        color = Color.Black
                    )
                }


                Button(
                    onClick = {
                        // 도시 룰렛 화면 으로 이동
                        viewModel.moveToRouletteCityScreen(scheduleTitle, scheduleStartDate, scheduleEndDate)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, // ✅ 버튼 배경색: 흰색
                        contentColor = Color(0xFF435C8F) // ✅ 버튼 텍스트 색상: 파란색 (변경 가능)
                    ),
                    shape = RectangleShape // ✅ 버튼을 사각형으로 변경
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.roulette_picture), // ✅ drawable 리소스 추가
                        contentDescription = "룰렛 이미지",
                        modifier = Modifier.size(70.dp).padding(end = 16.dp) // ✅ 아이콘 크기 조정 가능
                    )
                    Text(
                        text = "룰렛 돌리기",
                        fontFamily = NanumSquareRoundRegular,
                        fontSize = 35.sp,
                        color = Color.Black
                    )
                }


                // 도시 목록
                ScheduleCitySelectList(
                    dataList = viewModel.filteredCities,
                    scheduleTitle = viewModel.scheduleTitle.value,
                    scheduleStartDate = viewModel.scheduleStartDate.value,
                    scheduleEndDate = viewModel.scheduleEndDate.value,
                    onSelectedCity = {areaCode ->
                        // 일정 제목 날짜 아이디 저장 후 일정 상세로 넘어 간다
                        viewModel.addTripSchedule(
                            scheduleTitle = scheduleTitle,
                            scheduleStartDate = scheduleStartDate,
                            scheduleEndDate = scheduleEndDate,
                            areaName = areaCode.areaName,
                            areaCode = areaCode.areaCode
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