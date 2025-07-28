package com.lion.wandertrip.presentation.my_interesting_page.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lion.wandertrip.presentation.my_interesting_page.MyInterestingViewModel
import com.lion.wandertrip.util.CustomFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetAreaFilter(myInterestingViewModel: MyInterestingViewModel) {
    val localList by myInterestingViewModel.localList.collectAsState()
    val isSheetOpen by myInterestingViewModel.isSheetOpen.collectAsState()

    if (!isSheetOpen) return

    ModalBottomSheet(
        onDismissRequest = { myInterestingViewModel.setSheetOpen(false) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            localList.forEach { city ->
                Text(
                    text = city,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            myInterestingViewModel.setFilteredCity(city)
                            myInterestingViewModel.setSheetOpen(false)
                        }
                        .padding(16.dp),
                    fontFamily = CustomFont.customFontRegular
                )
            }
        }
    }
}