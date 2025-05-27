package com.lion.wandertrip.presentation.bottom_menu_page
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lion.wandertrip.R

import com.lion.wandertrip.presentation.bottom.home_page.HomeScreen
import com.lion.wandertrip.presentation.bottom.my_info_page.MyInfoScreen
import com.lion.wandertrip.presentation.bottom.schedule_page.ScheduleScreen
import com.lion.wandertrip.presentation.bottom.trip_note_page.TripNoteScreen
import com.lion.wandertrip.util.NavigationData

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomMenuScreen(bottomMenuViewModel: BottomMenuViewModel = hiltViewModel()) {
    val navMenus = listOf(
        NavigationData("홈", R.drawable.ic_home_24px),
        NavigationData("일정", R.drawable.ic_calendar_month_24px),
        NavigationData("여행기", R.drawable.ic_camera_film_24px),
        NavigationData("My", R.drawable.ic_person_24px)
    )


    /*var selectedItem by remember { mutableStateOf(0) }*/

    Scaffold(
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier
                        .border(1.dp, Color.White)  // 테두리를 흰색으로 설정
                        .background(Color.White),   // BottomAppBar 배경색을 흰색으로 설정
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround

                ) {
                    navMenus.forEachIndexed { index, menu ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { bottomMenuViewModel.tripApplication.selectedItem.value =  index }
                        ) {
                            // 🔹 drawable 리소스 아이콘 표시
                            Image(
                                painter = painterResource(id = menu.iconRes),
                                contentDescription = menu.title,
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(
                                    if (bottomMenuViewModel.tripApplication.selectedItem.value == index) Color(0xFF0077B6) // 클릭시 파란색
                                    else Color.Gray // 기본값 회색
                                )
                            )

                            Spacer(modifier = Modifier.padding(2.dp))

                            Text(
                                text = menu.title,
                                color = if (bottomMenuViewModel.tripApplication.selectedItem.value == index) Color.Black else Color.Gray
                            )
                        }
                    }

                }
            }
        }
    ) {
        // 스캐폴드에서 받은 패딩을 적용
        paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (bottomMenuViewModel.tripApplication.selectedItem.value) {
                0 -> HomeScreen() // ✅ HomeScreen으로 fillMaxSize()를 전달합니다.
                1 -> ScheduleScreen()
                2 -> TripNoteScreen()
                3 -> MyInfoScreen()
            }
        }
    }
}