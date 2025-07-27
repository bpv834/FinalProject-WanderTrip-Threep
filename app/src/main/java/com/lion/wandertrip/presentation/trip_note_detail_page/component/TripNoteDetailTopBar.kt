package com.lion.wandertrip.presentation.trip_note_detail_page.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.R
import com.lion.wandertrip.presentation.trip_note_detail_page.TripNoteDetailViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripNoteDetailTopBar(
    onNavigationClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    showDeleteIcon: Boolean
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        ),
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        },
        title = {},
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (showDeleteIcon) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.padding(start = 20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_24px),
                            contentDescription = "삭제"
                        )
                    }
                }

                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.padding(end = 3.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_download_24px),
                        contentDescription = "다운로드"
                    )
                }
            }
        }
    )
}