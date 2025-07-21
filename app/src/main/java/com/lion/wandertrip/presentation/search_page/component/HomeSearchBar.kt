package com.lion.wandertrip.presentation.search_page.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun HomeSearchBar(
    query: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onClearQuery: () -> Unit,
    onBackClicked: () -> Unit // 🔙 뒤로 가기 콜백 추가
) {
    var searchText by remember { mutableStateOf(query) }

    // ✅ 최근 검색어 클릭 시 searchQuery가 변경되면 반영
    LaunchedEffect(query) {
        searchText = query
    }

    OutlinedTextField(
        value = searchText,
        onValueChange = {
            searchText = it
            onSearchQueryChanged(it) // 검색어 변경 콜백
        },
        placeholder = { Text("지역명을 입력하세요") },

        // 🔙 뒤로 가기 아이콘 추가
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClicked) { // 뒤로 가기 클릭 시 동작
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로 가기")
                }
            }
        },

        // ❌ X 버튼 & 🔍 검색 아이콘 추가
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = {
                        searchText = ""
                        onClearQuery() // X 버튼 클릭 시 전체 목록 표시
                    }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
                IconButton(onClick = onSearchClicked) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
        },

        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchClicked() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 10.dp, start = 16.dp, end = 16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,     // 포커스 시 테두리 색상
            focusedContainerColor = Color(0xFFEFEFEF), // ✅ 포커스 상태일 때 배경색
            unfocusedBorderColor = Color.Transparent,   // 포커스 해제 시 테두리 색상
            unfocusedContainerColor = Color(0xFFEFEFEF) // ✅ 포커스 해제 시 배경색
        ),
        // 모서리 둥글게 설정
        shape = RoundedCornerShape(20.dp),
    )
}
