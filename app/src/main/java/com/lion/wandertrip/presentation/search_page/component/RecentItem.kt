package com.lion.wandertrip.presentation.search_page.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lion.wandertrip.presentation.search_page.SearchViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound

@Composable
fun RecentItem(searchViewModel: SearchViewModel) {
    val recentSearches by rememberUpdatedState(searchViewModel.recentSearches) // ✅ 상태 업데이트 반영

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // "최근 검색" 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "최근 검색", fontFamily = NanumSquareRound, fontSize = 18.sp)

            Text(
                text = "모두 삭제",
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = NanumSquareRound,
                modifier = Modifier
                    .clickable { searchViewModel.clearRecentSearches() }
                    .padding(4.dp)
            )
        }

        if (recentSearches.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(recentSearches) { search ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { searchViewModel.selectRecentSearch(search) } // ✅ 수정
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = search,
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                            fontFamily = NanumSquareRound
                        )

                        Spacer(modifier = Modifier.width(8.dp)) // 🔹 간격 추가

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "삭제",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { searchViewModel.removeRecentSearch(search) }
                        )
                    }
                }
            }
        } else {
            // 최근 검색어가 없을 경우
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "최근 검색어가 없습니다",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontFamily = NanumSquareRound,
                )
            }
        }
    }
}

