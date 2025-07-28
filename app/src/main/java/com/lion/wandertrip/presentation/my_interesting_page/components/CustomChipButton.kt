package com.lion.wandertrip.presentation.my_interesting_page.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.wandertrip.R

@Composable
fun CustomChipButton(text: String, onClick: () -> Unit,isClick : Boolean) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp), // 둥근 모서리 설정
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), // 배경을 투명으로 설정
        border = BorderStroke(1.dp, Color.Gray), // 테두리 추가
        modifier = Modifier
            .height(50.dp), // 버튼 높이 설정
        contentPadding = PaddingValues(horizontal = 10.dp) // 패딩 설정
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = text, fontSize = 15.sp, color = Color.Black) // 텍스트 표시
            Spacer(modifier = Modifier.width(4.dp)) // 텍스트와 아이콘 간격
            if(isClick)
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_check_24px), // 체크 아이콘
                contentDescription = "Chip Icon",
                tint = Color.Black
            )
        }
    }
}