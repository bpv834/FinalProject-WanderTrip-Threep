package com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item


import ScheduleItemSearchBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.R
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.schedule_detail_random_page.schedule_random_select_item.components.ScheduleRandomItemList
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleRandomSelectItemScreen(
    viewModel: ScheduleRandomSelectItemViewModel = hiltViewModel()
) {
    // 🔍 검색어 상태
    var searchQuery by remember { mutableStateOf("") }
    // 선택된 카태고리 상태
    var selectedCategoryCode by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading // ✅ 로딩 상태 가져오기

    val isFirstLaunch = rememberSaveable { mutableStateOf(true) } // ✅ 처음 실행 여부 저장

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
                        Text(text = viewModel.title.value, fontFamily = NanumSquareRound)
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.backScreen() }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "뒤로 가기")
                        }
                    },
                )
            }
        ) {
            Column(modifier = Modifier.padding(it)) {
                // 룰렛 이동 버튼
                Button(
                    onClick = { /*viewModel.moveToRouletteItemScreen(tripScheduleDocId, areaName, areaCode)*/ },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF435C8F)
                    ),
                    shape = RectangleShape
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.roulette_picture),
                        contentDescription = "룰렛 이미지",
                        modifier = Modifier.size(50.dp).padding(end = 16.dp)
                    )
                    Text(
                        text = "룰렛 돌리기",
                        fontFamily = NanumSquareRoundRegular,
                        fontSize = 25.sp,
                        color = Color.Black
                    )
                }

                CustomDividerComponent()


                ScheduleRandomItemList(
                    viewModel,
                    onClickAddSchedule = { selectItem -> viewModel.addTripItemToSchedule(selectItem) },
                    onClickShowItemDetail = {str->viewModel.moveToDetailScreen(str)}
                )
            }
        }

        // ✅ 로딩 화면 추가 (투명 오버레이)
        if (isLoading) {
            LottieLoadingIndicator() // ✅ 로딩 애니메이션
        }

    }
}