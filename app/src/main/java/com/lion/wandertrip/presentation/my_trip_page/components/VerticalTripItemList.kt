package com.lion.wandertrip.presentation.my_trip_page.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.lion.a02_boardcloneproject.component.CustomIconButton
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.presentation.my_trip_page.MyTripViewModel
import com.lion.wandertrip.util.CustomFont
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun VerticalTripItemList(myTripViewModel: MyTripViewModel) {
    Column(modifier = Modifier.fillMaxSize()
        .verticalScroll(rememberScrollState()) // 스크롤 가능하게 설정
        .padding(16.dp)) {
        // 다가오는 여행
        if(myTripViewModel.upComingTripList.size!=0)
        Column  {
            Text(text = "다가오는 여행", fontSize = 20.sp,  fontFamily = CustomFont.customFontBold)
            myTripViewModel.upComingTripList
                .forEachIndexed { index, tripScheduleModel ->
                    TripItem(trip = myTripViewModel.upComingTripList[index],myTripViewModel,index)
                }
        }

        Spacer(modifier = Modifier.height(16.dp)) // 두 섹션 간격
        if(myTripViewModel.pastTripList.size !=0)
        // 지난 여행
        Column {
            Text(text = "지난 여행", fontSize = 20.sp, fontFamily = CustomFont.customFontBold)
            myTripViewModel.pastTripList
                .forEachIndexed { index, tripScheduleModel ->
                    TripItem(trip = myTripViewModel.pastTripList[index],myTripViewModel,index+myTripViewModel.upComingTripList.size)
                }
        }
    }
}

@Composable
fun TripItem(trip: TripScheduleModel,myTripViewModel: MyTripViewModel,pos : Int) {
    Row(
        modifier = Modifier.clickable {
            myTripViewModel.onClickScheduleItemGoScheduleDetail(trip.tripScheduleDocId,trip.scheduleCity)
        }
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        // 이미지 자리
        Box(
            modifier = Modifier
                .size(60.dp) // 이미지 크기
                .background(Color.Gray) // 이미지 자리만 만든다고 가정
        )

        // 여행 정보 (지역명, 시작일, 종료일)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Text(text = "${trip.scheduleCity} 여행", fontFamily = CustomFont.customFontRegular)
            Text(
                text = "${formatDate(trip.scheduleStartDate)} - ${formatMonthDayDate(trip.scheduleEndDate)}",
                fontFamily = CustomFont.customFontLight
            )
        }

        // 팝업 메뉴 (점 3개 버튼)
        if(!myTripViewModel.menuStateMap[pos]!!)
        IconButton(onClick = {
            myTripViewModel.onClickIconMenu(pos)
        }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu"
            )
        }
        else{
            Row{
                // 여행 날짜 수정
                CustomIconButton(icon = ImageVector.vectorResource(R.drawable.ic_calendar_month_24px), iconButtonOnClick = {})
                // 여행 삭제
                CustomIconButton(icon = ImageVector.vectorResource(R.drawable.ic_delete_24px), iconButtonOnClick = {
                    myTripViewModel.onClickIconDeleteTrip(trip)
                })
            }
        }

    }
}

// 날짜 포맷 함수
fun formatDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

// 날짜 포맷 함수
fun formatMonthDayDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}