package com.lion.wandertrip.presentation.schedule_select_item

import ScheduleItemCategoryChips
import ScheduleItemSearchBar
import android.util.Log
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
import com.google.firebase.Timestamp
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.wandertrip.R
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.schedule_select_item.component.ScheduleItemList
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular
import com.lion.wandertrip.util.ContentTypeId
import com.lion.wandertrip.util.SharedTripItemList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSelectItemScreen(
    itemCode: Int,
    areaName: String,
    areaCode: Int,
    scheduleDate: Long,
    tripScheduleDocId: String,
    viewModel: ScheduleSelectItemViewModel = hiltViewModel()
) {
    // 🔍 검색어 상태
    var searchQuery by remember { mutableStateOf("") }
    // 선택된 카태고리 상태
    var selectedCategoryCode by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading // ✅ 로딩 상태 가져오기

    val isFirstLaunch = rememberSaveable { mutableStateOf(true) } // ✅ 처음 실행 여부 저장

    LaunchedEffect(Unit) {
        viewModel.scheduleDate.value = Timestamp(scheduleDate, 0)
        viewModel.tripScheduleDocId.value = tripScheduleDocId
        // viewModel.observeUserScheduleDocIdList()
        viewModel.observeUserLikeList()
        viewModel.observeContentsData()

        if (isFirstLaunch.value) { // ✅ 처음 실행될 때만 실행
            viewModel.loadTripItems(
                serviceKey = "ksezhUKKJp9M9RgOdmmu9i7lN1+AbkA1dk1xZpqMMam319sa3VIQHFtCXfADM1OxBUls7SrMrmun3AFTYRj5Qw==",
                areaCode = "$areaCode",
                contentTypeId = "$itemCode"
            )

            isFirstLaunch.value = false // ✅ 이후에는 실행되지 않도록 설정
        }
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
                        val title = when (itemCode) {
                            ContentTypeId.TOURIST_ATTRACTION.contentTypeCode -> "관광지"
                            ContentTypeId.RESTAURANT.contentTypeCode -> "음식점"
                            ContentTypeId.ACCOMMODATION.contentTypeCode -> "숙소"
                            else -> ""
                        }
                        Text(text = "$title 추가하기", fontFamily = NanumSquareRound)
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

                // 🔍 검색 바 추가
                ScheduleItemSearchBar(
                    query = searchQuery,
                    onSearchQueryChanged = { searchQuery = it },
                    onSearchClicked = {},
                    onClearQuery = { searchQuery = "" }
                )

      /*          // 🎯 카테고리 칩 버튼 추가
                ScheduleItemCategoryChips(
                    itemCode = itemCode,
                    selectedCategoryCode = selectedCategoryCode,
                    onCategorySelected = { newCategoryCode ->
                        selectedCategoryCode = newCategoryCode
                    }
                )*/

                // 룰렛 이동 버튼
                Button(
                    onClick = { viewModel.moveToRouletteItemScreen(tripScheduleDocId, areaName, areaCode) },
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

                Log.d("ScheduleSelectItemScreen", "itemCode : ${itemCode}")

                // ✅ 필터링된 여행지 리스트
                val filteredList = SharedTripItemList.sharedTripItemList.filter {
                    val matchesCategory = when (itemCode) {
                        12 -> selectedCategoryCode == null || it.cat2 == selectedCategoryCode
                        39, 32 -> selectedCategoryCode == null || it.cat3 == selectedCategoryCode
                        else -> true
                    }
                    val matchesSearchQuery = it.title.contains(searchQuery, ignoreCase = true)
                    matchesCategory && matchesSearchQuery
                }
                Log.d("ScheduleSelectItemScreen", "sharedTripItemList : ${SharedTripItemList.sharedTripItemList.size}")

                ScheduleItemList(
                    tripItemList = filteredList,
                    viewModel,
                    onItemClick = { selectItem -> viewModel.addTripItemToSchedule(selectItem) }
                )
            }
        }

        // ✅ 로딩 화면 추가 (투명 오버레이)
        if (isLoading) {
            LottieLoadingIndicator() // ✅ 로딩 애니메이션
        }

    }
}