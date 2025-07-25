package com.lion.wandertrip.presentation.bottom.home_page.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.lion.wandertrip.presentation.bottom.home_page.HomeViewModel
import com.lion.wandertrip.ui.theme.NanumSquareRound
import com.lion.wandertrip.model.PopularCityModel


@Composable
fun HorizontalPopularCityList(viewModel: HomeViewModel) {
    val popularCities by viewModel.tripApplication.popularCities.collectAsState()
    Column(
        modifier = Modifier.fillMaxWidth() // 컬럼이 가로로 가득 차도록 합니다.
    ) {
        // "인기 도시 top 10" 제목을 표시합니다.
        Text(
            text = "인기 도시 top 10",
            fontSize = 20.sp,
            fontFamily = NanumSquareRound,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        // 인기 도시 아이템들을 가로로 스크롤 가능한 목록으로 표시합니다.
        LazyRow(
            modifier = Modifier.fillMaxWidth(), // LazyRow가 가로로 가득 차도록 합니다.
            contentPadding = PaddingValues(horizontal = 16.dp), // LazyRow의 내용물에 좌우 패딩을 줍니다.
            horizontalArrangement = Arrangement.spacedBy(12.dp) // 아이템들 사이에 12dp의 간격을 줍니다.
        ) {
            // 뷰모델에서 가져온 인기 도시 목록을 반복하여 표시합니다.
            items(popularCities) { city ->
                PopularCityItemView(city = city,viewModel) // 각 도시 아이템을 표시하는 컴포저블을 호출합니다.
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class) // Glide Compose API를 사용하기 위한 옵트인입니다.
@Composable
fun PopularCityItemView(city: PopularCityModel, viewModel : HomeViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // 컬럼 내의 내용을 수평 중앙 정렬합니다.
        modifier = Modifier.width(80.dp) // 각 도시 아이템의 너비를 80dp로 고정하여 레이아웃을 일정하게 유지합니다.
            .clickable {
                Log.d("PopularCityItemView","${city.lat} ${city.lng} ${city.name}")
                viewModel.onClickPopularCity(city.lat,city.lng,city.name,city.radius.toString())
            }
    ) {
        // Glide를 사용하여 도시 이미지를 로드하고 표시합니다.
        GlideImage(
            model = city.imageUrl, // 로드할 이미지의 URL입니다.
            contentDescription = "${city.name} 도시 이미지", // 스크린 리더를 위한 접근성 설명입니다.
            contentScale = ContentScale.Crop, // 이미지가 잘리지 않도록 확대하여 표시합니다.
            modifier = Modifier
                .width(80.dp) // 이미지 너비를 60dp로 설정합니다.
                .height(80.dp) // 이미지 높이를 60dp로 설정합니다.
                .clip(CircleShape),
            // placeholder는 Glide 자체적으로 제공하는 방법을 사용하는 것이 더 좋습니다.
            // 예를 들어, .error() 또는 .loading()을 사용하여 로딩 중/오류 시 이미지를 지정할 수 있습니다.
        )
        // 도시의 순위와 이름을 표시합니다.
        Text(
            text = "${city.rank}. ${city.name}", // "순위. 도시이름" 형식으로 표시합니다.
            style = MaterialTheme.typography.bodySmall, // MaterialTheme의 작은 본문 텍스트 스타일을 사용합니다.
            fontWeight = FontWeight.Medium, // 글꼴 두께를 중간으로 설정합니다.
            modifier = Modifier.padding(top = 4.dp) // 이미지와 텍스트 사이에 상단 패딩을 추가합니다.
        )
    }
}