package com.lion.wandertrip.presentation.detail_review_write_page

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomIconButton
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.wandertrip.R
import com.lion.wandertrip.component.CustomDraggableRatingBar
import com.lion.wandertrip.component.LottieLoadingIndicator
import com.lion.wandertrip.presentation.detail_review_write_page.components.CustomBasicTextField
import com.lion.wandertrip.util.CustomFont
import com.lion.wandertrip.util.Tools
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun DetailReviewWriteScreen(
    contentID: String,
    contentTitle: String,
    detailReviewWriteViewModel: DetailReviewWriteViewModel = hiltViewModel()
) {

    Log.d("DetailReviewWriteScreen","DetailReviewWriteScreen")
    Log.d("test100","contentId : $contentID")
    Log.d("test100","contenttitle : $contentTitle")


    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val tripApplication = detailReviewWriteViewModel.tripApplication
    // 앨범용 런처 (여러 개 선택 가능)
    val albumLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList: List<Uri> ->
            if (uriList.isNotEmpty()) {
                Log.d("test100","uriList  : ${uriList.joinToString()}")

                Tools.getAlbumDataList(
                    tripApplication,
                    uriList,
                    detailReviewWriteViewModel.mutableBitMapList
                )
                detailReviewWriteViewModel.isImagePicked.value = true
            }
        }

   if(detailReviewWriteViewModel.isLoading.value){
       LottieLoadingIndicator()
   }else{
       Scaffold(
           topBar = {
               CustomTopAppBar(
                   title = contentTitle,
                   menuItems = {
                       // 작성 완료 아이콘
                       if (detailReviewWriteViewModel.reviewContentValue.value != "")
                           CustomIconButton(
                               ImageVector.vectorResource(R.drawable.ic_check_24px),
                               iconButtonOnClick = {
                                   detailReviewWriteViewModel.isLoading.value=true
                                   detailReviewWriteViewModel.addContentsReview(contentID,contentTitle)
                               }
                           )
                   },
                   navigationIconImage = Icons.AutoMirrored.Filled.ArrowBack,
                   // 뒤로가기 버튼
                   navigationIconOnClick = {
                       detailReviewWriteViewModel.onClickNavIconBack()
                   }
               )
           }
       ) {
           Column(
               modifier = Modifier
                   .padding(it)
                   .padding(20.dp)
                   .verticalScroll(scrollState)
                   .fillMaxWidth()
           ) {
               Column(
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(horizontal = 30.dp, vertical = 18.dp),
                   horizontalAlignment = Alignment.CenterHorizontally
               ) {
                   Spacer(modifier = Modifier.height(50.dp))
                   CustomDraggableRatingBar(
                       ratingState = detailReviewWriteViewModel.ratingScoreValue,
                       onRatingChanged = { newRating ->
                           // 별점이 변경될 때 처리하는 로직
                           println("New rating: $newRating")
                       }
                   )
                   Text(
                       "별점을 선택해주세요!",
                       color = Color.Gray,
                       fontFamily = CustomFont.customFontRegular,
                       fontSize = 18.sp
                   )
                   Spacer(modifier = Modifier.height(50.dp))
               }
               Spacer(modifier = Modifier.height(20.dp))

               CustomBasicTextField(
                   placeholder = "내용을 입력해 주세요",
                   textFieldValue = detailReviewWriteViewModel.reviewContentValue, // 뷰모델에서 텍스트 값 가져오기
               )
               Row(
                   modifier = Modifier
                       .fillMaxWidth()
                       .horizontalScroll(rememberScrollState()) // 가로 스크롤 적용
                       .padding(top = 16.dp)
               ) {
                   // 이미지 추가 버튼
                   Box(
                       modifier = Modifier
                           .size(80.dp) // 정사각형 크기 설정
                           .clip(RoundedCornerShape(8.dp)) // 모서리 둥글게 (원하면 값 조정 가능)
                           .background(Color.LightGray) // 회색 배경
                       ,
                       contentAlignment = Alignment.Center // 아이콘을 중앙 정렬
                   ) {
                       Icon(
                           imageVector = Icons.Default.Add,
                           contentDescription = "이미지 추가",
                           tint = Color.DarkGray, // 아이콘 색상 (더 진한 회색)
                           modifier = Modifier
                               .padding(vertical = 4.dp)
                               .size(24.dp)
                               .clickable {
                                   Log.d("DetailReviewWrite", "이미지 선택 버튼 클릭됨")  // 로그 확인
                                   // "image/*" MIME 타입을 전달하면, 앨범에서 이미지 파일만 선택할 수 있도록 제한합니다.
                                   albumLauncher.launch("image/*") // 여러 개 선택 가능하도록 설정
                               } // 아이콘 크기
                       )
                   }
                   // 예제 이미지 리스트
                   detailReviewWriteViewModel.mutableBitMapList.forEachIndexed { idx, imageRes ->
                       Log.d("test100","imageRes : ${imageRes} ")
                       Box(
                           modifier = Modifier
                               .padding(end = 4.dp)
                               .size(80.dp) // 이미지 크기 설정
                               .clip(RoundedCornerShape(8.dp)) // 이미지 둥글게 만들기
                               .background(Color.LightGray) // 기본 배경색
                       ) {
                           GlideImage(
                               imageModel = imageRes,
                               contentScale = ContentScale.Crop,
                               modifier = Modifier.fillMaxSize(), // 이미지가 Box를 채우도록 설정
                               circularReveal = CircularReveal(duration = 250),
                               placeHolder = ImageBitmap.imageResource(R.drawable.img_image_holder)
                           )

                           // X 아이콘 추가 (이미지 우측 상단)
                           Icon(
                               imageVector = Icons.Default.Close,  // X 아이콘
                               contentDescription = "삭제",
                               modifier = Modifier
                                   .align(Alignment.TopEnd)  // 우측 상단에 위치
                                   .clickable {
                                       // 이미지 삭제
                                       detailReviewWriteViewModel.mutableBitMapList.remove(imageRes)
                                   },
                               tint = Color.Gray  // X 아이콘 색상
                           )
                       }
                   }
               }
           }
       }
   }
}