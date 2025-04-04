package com.lion.wandertrip.presentation.bottom.schedule_page.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.presentation.bottom.schedule_page.ScheduleViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.ui.theme.NanumSquareRoundRegular

@Composable
fun ScheduleItemList(
    dataList: List<TripScheduleModel>,
    viewModel: ScheduleViewModel = hiltViewModel(),
    scheduleType: Int,
    onRowClick: (TripScheduleModel) -> Unit = {}, // 클릭 이벤트 추가
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) { // RecyclerView 대체
        items(dataList.size) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 카드 그림자 효과
                shape = RoundedCornerShape(16.dp), // 라운드 모서리 설정
                colors = CardDefaults.cardColors(Color(0xFFFFFFFF))
            ) {
                Column( // 클릭 불필요 → 카드 내부에서만 클릭 감지
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onRowClick(dataList[index]) },
                ) {
                    Column( // 클릭 불필요 → 카드 내부에서만 클릭 감지
                        modifier = Modifier.fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            // 일정 제목
                            Text(
                                text = dataList[index].scheduleTitle,
                                fontFamily = NanumSquareRound,
                                fontSize = 20.sp,
                                modifier = Modifier.weight(1f)
                            )

                            ScheduleDropDawnIconButton(
                                icon = Icons.Filled.MoreVert,
                                size = 15,
                                menuItems = listOf("삭제"),
                                dataList[index].tripScheduleDocId,
                                onDeleteSchedule = { scheduleDocId ->
                                    if (scheduleType == 0) {
                                        // 참가자가 1명인경우 일정 컬렉션에서도 문서를 삭제/ 그게 아니라면 user 서브컬렉션에서만 삭제
                                        if(dataList[index].scheduleInviteList.size<2){
                                            viewModel.deleteScheduleIfZeroParty(dataList[index])
                                        }
                                        viewModel.removeUserSchedule(scheduleDocId)
                                    } else {
                                        viewModel.removeInvitedSchedule(scheduleDocId)
                                    }
                                }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp),
                            verticalAlignment = Alignment.CenterVertically // ✅ 수직 중앙 정렬
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person, // ✅ 머티리얼 아이콘 사용
                                contentDescription = "함께 하는 사람 아이콘",
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .size(12.5.dp)  // ✅ 크기 조절
                            )

                            // 참가 인원 수
                            Text(
                                text = dataList[index].scheduleInviteList.size.toString(),
                                fontFamily = NanumSquareRound,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(end = 5.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp),
                            verticalAlignment = Alignment.CenterVertically // ✅ 수직 중앙 정렬
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Place, // ✅ 머티리얼 아이콘 사용
                                contentDescription = "홈 아이콘",
                                modifier = Modifier
                                    .size(12.5.dp)  // ✅ 크기 조절
                            )

                            // 일정  지역
                            Text(
                                text = dataList[index].scheduleCity,
                                fontFamily = NanumSquareRoundRegular,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(end = 5.dp)
                            )

                            // 일정 날짜
                            Text(
                                text = "${viewModel.formatTimestampToDateString(dataList[index].scheduleStartDate)} " +
                                        "~" +
                                        " ${viewModel.formatTimestampToDateString(dataList[index].scheduleEndDate)}",
                                fontFamily = NanumSquareRoundRegular,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 0.5.dp),
                            )
                        }
                        
                    }

                }
            }
        }
    }
}