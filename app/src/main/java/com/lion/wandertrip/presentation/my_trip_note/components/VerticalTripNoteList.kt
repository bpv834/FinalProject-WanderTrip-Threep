package com.lion.wandertrip.presentation.my_trip_note.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleCoroutineScope
import coil.compose.rememberImagePainter
import com.lion.a02_boardcloneproject.component.CustomIconButton
import com.lion.wandertrip.R
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.presentation.my_trip_note.MyTripNoteViewModel
import com.lion.wandertrip.util.CustomFont
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun VerticalTripNoteList(tripNoteList : MutableList<TripNoteModel>,myTripNoteViewModel: MyTripNoteViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(tripNoteList) { tripNoteModel ->
            val index = myTripNoteViewModel.tripNoteList.indexOf(tripNoteModel)
            TripNoteItem(tripNote = tripNoteModel, pos = index, myTripNoteViewModel)
        }
    }
}


@Composable
fun TripNoteItem(tripNote: TripNoteModel, pos: Int, myTripNoteViewModel: MyTripNoteViewModel) {
    val sH = myTripNoteViewModel.tripApplication.screenHeight
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp).padding(vertical = 5.dp)
            .clickable {
                myTripNoteViewModel.onClickTripNoteItemGoTripNoteDetail(tripNote.tripNoteDocumentId)
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // 여행기 제목
            Text(
                modifier = Modifier
                    .padding(bottom = 50.dp, top = 15.dp)
                    .padding(horizontal = 10.dp),
                text = tripNote.tripNoteTitle,
                fontFamily = CustomFont.customFontBold,
                fontSize = 24.sp, // 글씨 크기 설정
                maxLines = 1, // Limit text to a single line
                overflow = TextOverflow.Ellipsis // Add ellipsis when text overflows
            )

            Column(modifier = Modifier.padding(horizontal = 10.dp)) {

                GlideImage(
                    imageModel = if(tripNote.tripNoteImage[0]=="") ImageVector.vectorResource(R.drawable.img_image_holder) else tripNote.tripNoteImage[0],
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((sH / 12).dp),
                    circularReveal = CircularReveal(duration = 250),
                    placeHolder = ImageBitmap.imageResource(R.drawable.img_image_holder),
                )
                // 아이콘과 텍스트
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 좌측 아이콘들 (좋아요, 채팅)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "ScrapCount")
                        Text(
                            text = "${tripNote.tripNoteScrapCount}개",
                            modifier = Modifier.padding(start = 8.dp),
                            fontFamily = CustomFont.customFontRegular
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    // 우측 날짜와 팝업 메뉴
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(15.dp)
                    ) {
                        when (myTripNoteViewModel.calculationDate(tripNote.tripNoteTimeStamp)) {
                            0 -> {
                                Text(text = "오늘", fontFamily = CustomFont.customFontRegular)
                            }

                            1 -> {
                                Text(text = "하루 전", fontFamily = CustomFont.customFontRegular)
                            }

                            2 -> {
                                Text(text = "이틀 전", fontFamily = CustomFont.customFontRegular)
                            }

                            3 -> {
                                Text(text = "사흘 전", fontFamily = CustomFont.customFontRegular)
                            }

                            4 -> {
                                Text(text = "나흘 전", fontFamily = CustomFont.customFontRegular)
                            }

                            else -> {
                                Text(
                                    text = "${myTripNoteViewModel.calculationDate(tripNote.tripNoteTimeStamp)} 일전",
                                    fontFamily = CustomFont.customFontRegular
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        if (!myTripNoteViewModel.menuStateMap[pos]!!) {
                            // 메뉴 아이콘
                            CustomIconButton(iconButtonOnClick = {
                                myTripNoteViewModel.onClickIconMenu(pos)
                            }, icon = Icons.Default.MoreVert)
                        } else {
                            // 삭제 아이콘
                            CustomIconButton(
                                iconButtonOnClick = {
                                    myTripNoteViewModel.deleteTripNoteByDocId(tripNote.tripNoteDocumentId)
                                },
                                icon = ImageVector.vectorResource(R.drawable.ic_delete_24px)
                            )
                            // 수정 할 거면 밑에 수정 아이콘 추가
                        }
                    }
                }
            }
        }
    }
}
