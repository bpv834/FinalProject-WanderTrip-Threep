package com.lion.wandertrip.presentation.schedule_city_select.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.lion.wandertrip.R
import com.lion.wandertrip.presentation.schedule_city_select.ScheduleCitySelectViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular

@Composable
fun CityRouletteButton(
    viewModel: ScheduleCitySelectViewModel, scheduleTitle: String,
    scheduleStartDate: Timestamp,
    scheduleEndDate: Timestamp
) {
    Button(
        onClick = {
            // 도시 룰렛 화면 으로 이동
            viewModel.moveToRotateMapScreen(
                scheduleTitle,
                scheduleStartDate,
                scheduleEndDate
            )
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
            modifier = Modifier
                .size(70.dp)
                .padding(end = 16.dp) // ✅ 아이콘 크기 조정 가능
        )
        Text(
            text = "한반도 돌리기",
            fontFamily = NanumSquareRoundRegular,
            fontSize = 35.sp,
            color = Color.Black
        )
    }
}