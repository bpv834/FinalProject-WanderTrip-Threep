package com.lion.wandertrip.presentation.detail_page

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomIconButton
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.R
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.detail_page.components.BasicInfoDescriptionColumn
import com.lion.wandertrip.presentation.detail_page.components.BottomSheetAddSchedule
import com.lion.wandertrip.presentation.detail_page.components.BottomSheetReviewFilter
import com.lion.wandertrip.presentation.detail_page.components.IndicatorButton
import com.lion.wandertrip.presentation.detail_page.components.IntroColumn
import com.lion.wandertrip.presentation.detail_page.components.ReviewLazyColumn
import com.lion.wandertrip.presentation.detail_page.components.ViewGoogleMap
import com.lion.wandertrip.util.CustomFont


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(contentID: String, detailViewModel: DetailViewModel = hiltViewModel()) {
    Log.d("test", "contentID : $contentID")

    val toastMessage = detailViewModel.toastMessageValue.value

    LaunchedEffect(Unit) {
        detailViewModel.getCommonTripContentModel(contentID)
    }

    // 토스트 내용이 변할때마다 동작하는 런쳐
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(detailViewModel.tripApplication, it, Toast.LENGTH_SHORT).show()
        }
    }


   if(detailViewModel.isLoading.value){
       LottieLoadingIndicator()
   }else{
       if(!detailViewModel.isLoading.value)
       Scaffold(
           //containerColor = Color.White,
           topBar = {
               CustomTopAppBar(
                   title = "상세페이지",
                   menuItems = {
                       // 위치 아이콘
                       CustomIconButton(
                           ImageVector.vectorResource(R.drawable.ic_location_on_24px),
                           iconButtonOnClick = {
                               detailViewModel.onClickIconMap(contentID)
                           }
                       )
                       /*// 일정 추가 아이콘
                       CustomIconButton(
                           ImageVector.vectorResource(R.drawable.ic_calendar_add_on_24px),
                           iconButtonOnClick = {
                               detailViewModel.onClickIconAddSchedule()
                           }
                       )*/
                   },
                   navigationIconImage = Icons.AutoMirrored.Filled.ArrowBack,
                   navigationIconOnClick = {
                       detailViewModel.onClickNavIconBack()
                   }
               )
           }
       ) {
           LazyColumn(
               modifier = Modifier
                   .background(Color.White)
                   .fillMaxSize()
                   .padding(it)
                   .padding(horizontal = 12.dp),
               horizontalAlignment = Alignment.CenterHorizontally // 가로 정렬 중앙
           ) {
               // 스크롤 시 고정되는 헤더
               stickyHeader {
                   Column(
                       modifier = Modifier
                           .fillMaxWidth()
                           .background(Color.White) // 배경 색상 설정
                           .zIndex(1f) // zIndex를 주어 다른 아이템 위로 고정
                   ) {
                       Row(
                           modifier = Modifier.fillMaxWidth(),
                           horizontalArrangement = Arrangement.SpaceAround
                       ) {
                           IndicatorButton(
                               { detailViewModel.onClickButtonIntro() },
                               "소개",
                               ImageVector.vectorResource(R.drawable.ic_indicator_24px),
                               detailViewModel.isClickIntroState
                           )
                           IndicatorButton(
                               { detailViewModel.onClickButtonBasicInfo() },
                               "기본정보",
                               ImageVector.vectorResource(R.drawable.ic_indicator_24px),
                               detailViewModel.isClickBasicInfoState
                           )
                           IndicatorButton(
                               { detailViewModel.onClickButtonReview() },
                               "후기",
                               ImageVector.vectorResource(R.drawable.ic_indicator_24px),
                               detailViewModel.isClickReviewState
                           )
                       }
                   }
               }

               // 뷰페이저 항목
               if (detailViewModel.isClickIntroState.value)
               // 소개 페이지
                   item { IntroColumn(detailViewModel, contentID) }
               if (detailViewModel.isClickBasicInfoState.value)
                   item {
                       // 기본 정보 페이지
                       Column {
                           Text("기본정보", fontSize = 30.sp, fontFamily = CustomFont.customFontBold)
                           Spacer(modifier = Modifier.height(32.dp)) // 간격
                           ViewGoogleMap(detailViewModel)
                           BasicInfoDescriptionColumn(detailViewModel)
                       }
                   }

               if (detailViewModel.isClickReviewState.value)
                   item {
                       // 후기 페이지
                       ReviewLazyColumn(detailViewModel)
                   }

               item {
                   Spacer(modifier = Modifier.height(32.dp)) // 간격
               }
           }
       }
   }

    // 리뷰 필터 BottomSheet가 표시될 때의 설정
    if (detailViewModel.isReviewOptionSheetOpen.value) {
        BottomSheetReviewFilter(detailViewModel)
    }

    // 일정 추가 BottomSheet가 표시될 때의 설정
    if (detailViewModel.isAddScheduleSheetOpen.value) {
        BottomSheetAddSchedule(detailViewModel)
    }

}