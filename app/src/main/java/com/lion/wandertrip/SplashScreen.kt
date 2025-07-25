package com.lion.wandertrip

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.wandertrip.ui.theme.WanderTripTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WanderTripTheme {
                SplashScreen(
                    onNavigate = {
                        startActivity(
                            Intent(this@SplashActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                        finish() // startActivity 이후에 호출
                    }
                )
            }
        }
    }


    @Composable
    fun SplashScreen(
        viewModel: SplashViewModel = hiltViewModel(),
        onNavigate: () -> Unit
    ) {
        val alpha = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            // LaunchedEffect를 사용하고 있기 때문에, 그 내부는 suspend 환경이므로 suspend 함수 호출이 가능합니다.
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(1500)
            )
            viewModel.savePopularCities()
            viewModel.fetchTripItemModel()
            onNavigate()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val painter = painterResource(R.drawable.logo_wander_trip)

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .width(200.dp)
                        .height(93.dp)
                        .alpha(alpha.value),
                    painter = painter,
                    contentDescription = "Welluga Staff",
                )
            }
        }
    }
}
// 출처: https://dev-inventory.com/52#1. 안드로이드 12 이상에서 제공하는 SplashScreen API의 장단점-1 [개발자가 들려주는 IT 이야기:티스토리]