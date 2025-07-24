package com.lion.wandertrip.presentation.schedule_city_select.city_roulette.roulette_city_select

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.presentation.schedule_city_select.city_roulette.RouletteCityViewModel
import com.lion.wandertrip.presentation.schedule_city_select.city_roulette.roulette_city_select.compenent.RouletteCitySelectChips
import com.lion.wandertrip.presentation.schedule_city_select.city_roulette.roulette_city_select.compenent.RouletteCitySelectList
import com.lion.wandertrip.presentation.schedule_city_select.component.ScheduleCitySelectSearchBar
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.util.RouletteScreenName

@Composable
fun RouletteCitySelectScreen(
    navController: NavController, // ✅ navController 직접 받음
    viewModel: RouletteCitySelectViewModel = hiltViewModel(),
    rouletteCityViewModel: RouletteCityViewModel = hiltViewModel(navController.getBackStackEntry("${RouletteScreenName.ROULETTE_CITY_SCREEN.name}?" +
            "scheduleTitle={scheduleTitle}&scheduleStartDate={scheduleStartDate}&scheduleEndDate={scheduleEndDate}")),
) {
    // 검색어
    var searchQuery by remember { mutableStateOf("") }

    viewModel.selectedCities.clear()
    viewModel.selectedCities.addAll(rouletteCityViewModel.cities)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CustomTopAppBar(
                title = "룰렛 항목 추가",
                navigationIconImage = Icons.Filled.ArrowBack,
                navigationIconOnClick = { navController.popBackStack() }, // ✅ 뒤로가기 구현
            )
        },
        bottomBar = { // ✅ 하단 버튼 추가
            BottomAppBar(
                containerColor = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            Log.d("RouletteCitySelectScreen", "선택된 도시 리스트: ${viewModel.selectedCities}")

                            // ✅ 기존 리스트를 유지하면서 값만 변경 (새로운 리스트 할당 X)
                            rouletteCityViewModel.cities.clear()
                            rouletteCityViewModel.cities.addAll(viewModel.selectedCities)

                            Log.d("RouletteCitySelectScreen", "업데이트된 룰렛 도시 리스트: ${rouletteCityViewModel.cities}")

                            navController.popBackStack() // ✅ 기존 B로 돌아가기 (ViewModel 유지됨)
                        },
                        enabled = viewModel.selectedCities.isNotEmpty(), // ✅ 선택된 항목이 없으면 비활성화
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "추가 하기",
                            fontFamily = NanumSquareRoundRegular
                        )
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            // 검색 바
            ScheduleCitySelectSearchBar(
                query = searchQuery,
                onSearchQueryChanged = {
                    searchQuery = it
                    viewModel.updateFilteredCities(it) // ✅ 검색어 변경 시 필터링 실행
                },
                onSearchClicked = { Log.d("RouletteCitySelectScreen", "검색 실행: $searchQuery") },
                onClearQuery = {
                    searchQuery = ""
                    viewModel.updateFilteredCities("") // ✅ X 버튼 클릭 시 전체 도시 목록 복원
                }
            )

            // ★ 전체 선택 버튼 추가 (검색 바와 Chip 사이에 배치)
            Button(
                onClick = {
                    // filteredCities 기준으로 전체 선택 (검색 결과에 해당하는 도시 모두 선택)
                    viewModel.selectedCities.clear()
                    viewModel.selectedCities.addAll(viewModel.filteredCities)
                    Log.d("RouletteCitySelectScreen", "전체 선택 버튼 클릭: 선택된 도시 수 = ${viewModel.selectedCities.size}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                // 모서리 둥글게 설정
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("도시 전체 선택", fontFamily = NanumSquareRoundRegular)
            }

            // ✅ 선택된 도시 목록을 Chip으로 표시
            if (viewModel.selectedCities.isNotEmpty()) {
                RouletteCitySelectChips(
                    selectedCities = viewModel.selectedCities,
                    onRemoveCity = { city ->
                        viewModel.selectedCities.remove(city)
                    }
                )
            }

            // 도시 목록
            RouletteCitySelectList(
                dataList = viewModel.filteredCities,
                selectedData = viewModel.selectedCities,
                onSelectedCity = {
                    viewModel.selectedCities.add(it)
                    Log.d("RouletteCitySelectScreen", "선택된 도시: ${viewModel.selectedCities}")
                }
            )
        }
    }
}