package com.lion.wandertrip.presentation.popular_city_page.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
@Composable
fun PageNavigationBar(
    pageFlow: StateFlow<Int>,
    totalPages: Int,
    onFirstPageClick: () -> Unit,
    onLastPageClick: () -> Unit,
    onPrevPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
    onNumberClick: () -> Unit
) {
    val currentPage by pageFlow.collectAsState()
    val buttonShape = RoundedCornerShape(6.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        NavButton("<<", onClick = onFirstPageClick)
        NavButton("<", onClick = onPrevPageClick)

        val maxVisiblePages = 5

        val end = currentPage.coerceAtMost(totalPages)
        val start = (end - maxVisiblePages + 1).coerceAtLeast(1)

        (start..end).forEach { page ->
            val isSelected = page == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(buttonShape)
                    .background(if (isSelected) Color(0xFF1976D2) else Color.White)
                    .border(1.dp, Color.Gray, buttonShape)
                    .clickable { onNumberClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = page.toString(),
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        NavButton(">", onClick = onNextPageClick)
        NavButton(">>", onClick = onLastPageClick)
    }
}

@Composable
private fun NavButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.LightGray)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Black)
    }
}