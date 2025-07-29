package com.lion.wandertrip.presentation.my_interesting_page

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.R
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.my_interesting_page.components.BottomSheetAreaFilter
import com.lion.wandertrip.presentation.my_interesting_page.components.CityDropdownButton
import com.lion.wandertrip.presentation.my_interesting_page.components.CustomChipButton
import com.lion.wandertrip.presentation.my_interesting_page.components.VerticalUserInterestingList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInterestingScreen(
    myInterestingViewModel: MyInterestingViewModel = hiltViewModel()
) {
   // Log.d("test", "MyInterestingScreen")

    val scrollState = rememberScrollState()
 /*   LaunchedEffect (Unit){
        myInterestingViewModel.getInterestingList()
    }*/
    // navController.currentBackStackEntry ->  그냥 현재 백스택 정보를 한번 가져오는 용도, 화면이 바뀌거나 파라미터가 바뀌어도 UI는 리컴포지션되지 않음
    val currentBackStackEntry = myInterestingViewModel.tripApplication.navHostController.currentBackStackEntryAsState().value // 화면 전환, 파라미터 변화 등으로 entry가 바뀌면 리컴포지션 유발
    // 역할은 NavBackStackEntry의 savedStateHandle에 저장된 Boolean 값(refresh_needed)을 Compose에서 observe할 수 있도록 상태로 변환
    val refresh = currentBackStackEntry?.savedStateHandle
        ?.getLiveData<Boolean>("refresh_needed")
        ?.observeAsState() // LiveData → Compose의 State로 변환

    LaunchedEffect(refresh?.value) {
        if (refresh?.value == true) {
            myInterestingViewModel.getInterestingList()  // 리프레시 처리
            currentBackStackEntry.savedStateHandle["refresh_needed"] = false
        }
    }
    // StateFlow 수신
    val isLoading by myInterestingViewModel.isLoading.collectAsStateWithLifecycle()
    val isSheetOpen by myInterestingViewModel.isSheetOpen.collectAsStateWithLifecycle()
    val isCheckAttraction by myInterestingViewModel.isCheckAttraction.collectAsStateWithLifecycle()
    val isCheckRestaurant by myInterestingViewModel.isCheckRestaurant.collectAsStateWithLifecycle()
    val isCheckAccommodation by myInterestingViewModel.isCheckAccommodation.collectAsStateWithLifecycle()
    val filteredList by myInterestingViewModel.interestingListFiltered.collectAsStateWithLifecycle()


    if (isLoading) {
    //    Log.d("test", "로딩중")
        LottieLoadingIndicator()
    } else {
    //    Log.d("test", "로딩중아님")
        Scaffold(
            topBar = {
                CustomTopAppBar(
                    navigationIconOnClick = { myInterestingViewModel.onClickNavIconBack() },
                    navigationIconImage = ImageVector.vectorResource(R.drawable.ic_arrow_back_24px),
                    title = "내 저장"
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CityDropdownButton(myInterestingViewModel) {
                        myInterestingViewModel.onClickCityDropdown()
                    }
                    CustomChipButton("관광지", {
                        myInterestingViewModel.toggleAttraction()
                    }, isCheckAttraction)
                    CustomChipButton("식당", {
                        myInterestingViewModel.toggleRestaurant()
                    }, isCheckRestaurant)
                    CustomChipButton("숙소", {
                        myInterestingViewModel.toggleAccommodation()
                    }, isCheckAccommodation)
                }

                val likeMap by myInterestingViewModel.likeMap.collectAsStateWithLifecycle()
                VerticalUserInterestingList(
                    viewModel = myInterestingViewModel,
                    items = filteredList,
                    likeMap = likeMap
                )
            }

            if (isSheetOpen) {
                BottomSheetAreaFilter(myInterestingViewModel)
            }
        }
    }
}