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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.model.UserModel
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
    val tripItems by viewModel.randomTourItems.observeAsState(emptyList())
    val topTrips by viewModel.topScrapedTrips.observeAsState(emptyList())


    val imageUrlMap = viewModel.imageUrlMap
    val isLoading by viewModel.isLoading.observeAsState(false) // ✅ 로딩 상태 감지
    val userModel by viewModel.userModel.observeAsState(UserModel(userDocId = "", userLikeList = emptyList()))
    val contentsModelMap by viewModel.contentsModelMap.collectAsState()
    // 좋아요 map 구독 변수
    val favoriteMap by viewModel.favoriteMap.collectAsState()

    LaunchedEffect(Unit) {
        // 트립 노트 가져오기
        viewModel.fetchTripNotes()
        // 스크랩 높은 여행기 가져오기
        viewModel.getTopScrapedTrips()
        /*viewModel.fetchRandomTourItems()*/
        // 유저 좋아요 목록 content ID 가져오기
        viewModel.loadFavorites()
    }

    LaunchedEffect (favoriteMap.size){
        favoriteMap.keys.forEach {
            Log.d("test100","key: $it")
            viewModel.fetchContentsModel(it)
        }
    }

    val navController = viewModel.tripApplication.navHostController
    var backStackRoutes by remember { mutableStateOf<List<String>>(emptyList()) }
    val scrollState = rememberScrollState()

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collectLatest { backStackEntry ->
            // 현재 백스택을 안전하게 가져옴
            val backStackList = navController.currentBackStack.value.mapNotNull { it.destination.route }

            backStackRoutes = backStackList // 최신 백스택 반영
        }
    }

    // 백스택 로그 출력
    LaunchedEffect(backStackRoutes) {
        Log.d("BackStack", "===== Current BackStack =====")
        backStackRoutes.forEach { route ->
            Log.d("BackStack", "Route: $route")
        }
        Log.d("BackStack", "=============================")
    }

    if (isLoading) {
        // ✅ 로딩 중일 때 표시할 화면
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LottieLoadingIndicator() // ✅ 로딩 UI 표시
        }
    } else {
        // ✅ 로딩 완료 후 실제 화면 표시
        Scaffold(
            containerColor = Color.White,
            topBar = {
                CustomTopAppBar(menuItems = {
                    IconButton(
                        onClick = { viewModel.onClickIconSearch() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "검색",
                        )
                    }
                })
            }
        ) {
            Column(
                modifier = Modifier.padding(it)
                    .fillMaxSize().verticalScroll(scrollState)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "인기 관광지",
                            fontSize = 20.sp,
                            fontFamily = NanumSquareRound,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(viewModel.tripApplication.popularTripList) { tripItem ->
                        TripSpotItem(
                            tripItem = tripItem,
                            onItemClick = { viewModel.onClickTrip(tripItem.contentId) },
                            userModel = userModel,
                            contentsModel = contentsModelMap[tripItem.contentId],
                            onFavoriteClick = { contentId -> viewModel.toggleFavorite(contentId) },
                            viewModel = viewModel,
                            isFavorite = favoriteMap[tripItem.contentId]?:false ,
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
                    items(topTrips) { tripNote ->
                        PopularTripNoteItem(
                            tripItem = tripNote,
                            imageUrl = imageUrlMap[tripNote.tripNoteImage.firstOrNull()],
                            onItemClick = { viewModel.onClickTripNote(tripNote.tripNoteDocumentId) }
                        )
                    }
                }
            }
        }
    }
}