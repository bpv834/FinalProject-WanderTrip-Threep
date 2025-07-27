package com.lion.wandertrip.presentation.bottom.home_page

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.presentation.bottom.home_page.components.HorizontalPopularCityList
import com.lion.wandertrip.presentation.bottom.home_page.components.PopularTripNoteItem
import com.lion.wandertrip.presentation.bottom.home_page.components.TripSpotItem
import com.lion.wandertrip.ui.theme.NanumSquareRound
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    /*    val tripItems by viewModel.randomTourItems.observeAsState(emptyList())*/
    val topTripNotes by viewModel.topScrapedTrips.observeAsState(emptyList())

    val popularTripList by viewModel.tripApplication.popularTripList.collectAsState()

    /*    val imageUrlMap = viewModel.imageUrlMap*/
    val isLoading by viewModel.isLoading.observeAsState(false) // ✅ 로딩 상태 감지
    val userModel by viewModel.userModel.observeAsState(
        UserModel(
            userDocId = "",
            userLikeList = emptyList()
        )
    )
    val contentsModelMap by viewModel.contentsModelMap.collectAsState()
    // 좋아요 map 구독 변수
    val favoriteMap by viewModel.favoriteMap.collectAsState()

    LaunchedEffect(Unit) {
        // 스크랩 높은 여행기 가져오기
        viewModel.getTopScrapedTrips()
        // 유저 좋아요 목록 content ID 가져오기
        viewModel.loadFavorites()

    }

    LaunchedEffect(favoriteMap.size) {
        favoriteMap.keys.forEach {
            Log.d("test100", "key: $it")
            viewModel.fetchContentsModel(it)
        }
    }

    val scrollState = rememberScrollState()
    val navController = viewModel.tripApplication.navHostController
    var backStackRoutes by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collectLatest { backStackEntry ->
            // 현재 백스택을 안전하게 가져옴
            val backStackList =
                navController.currentBackStack.value.mapNotNull { it.destination.route }

            backStackRoutes = backStackList // 최신 백스택 반영
        }
    }

/*
    // 백스택 로그 출력
    LaunchedEffect(backStackRoutes) {
        Log.d("BackStack", "===== Current BackStack =====")
        backStackRoutes.forEach { route ->
            Log.d("BackStack", "Route: $route")
        }
        Log.d("BackStack", "=============================")
    }
*/

    if (isLoading) {
        // ✅ 로딩 중일 때 표시할 화면
        LottieLoadingIndicator() // ✅ 로딩 UI 표시
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp), // 탑바 높이와 비슷하게 설정하여 검색 아이콘의 수직 위치를 조정합니다.
                        // 이미지상 검색 아이콘 위에 여백이 있는 것 같으므로, 필요에 따라 이 높이를 조절하거나
                        // Modifier.padding(top = ...)을 추가할 수 있습니다.
                        horizontalArrangement = Arrangement.End, // 아이콘을 우측 끝으로 정렬
                        verticalAlignment = Alignment.CenterVertically // 아이콘을 수직 중앙 정렬
                    ) {
                        IconButton(
                            onClick = { viewModel.onClickIconSearch() }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "검색",
                            )
                        }
                    }
                }

                item {
                    HorizontalPopularCityList(viewModel)
                }

                item {
                    Text(
                        text = "인기 관광지",
                        fontSize = 20.sp,
                        fontFamily = NanumSquareRound,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(popularTripList) { tripItem ->
                    TripSpotItem(
                        tripItem = tripItem,
                        onItemClick = { viewModel.onClickTrip(tripItem.contentId) },
                        userModel = userModel,
                        contentsModel = contentsModelMap[tripItem.contentId],
                        onFavoriteClick = { contentId -> viewModel.toggleFavorite(contentId) },
                        viewModel = viewModel,
                        isFavorite = favoriteMap[tripItem.contentId] ?: false,
                    )
                }
                item {
                    Text(
                        text = "🔥 인기 많은 여행기",
                        fontSize = 20.sp,
                        fontFamily = NanumSquareRound,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(topTripNotes) { tripNote ->
                    PopularTripNoteItem(
                        tripItem = tripNote,
                        onItemClick = { viewModel.onClickTripNote(tripNote.tripNoteDocumentId) }
                    )
                }
            }
        }
    }
}
