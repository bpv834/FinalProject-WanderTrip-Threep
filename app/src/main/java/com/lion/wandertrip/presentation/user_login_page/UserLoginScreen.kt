package com.lion.wandertrip.presentation.user_login_page

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomCheckBox
import com.lion.a02_boardcloneproject.component.LikeLionOutlinedTextFieldEndIconMode
import com.lion.a02_boardcloneproject.component.LikeLionOutlinedTextFieldInputType
import com.lion.a02_boardcloneproject.component.CustomAlertDialog
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.a02_boardcloneproject.component.CustomDividerComponent
import com.lion.a02_boardcloneproject.component.CustomOutlinedButton
import com.lion.a02_boardcloneproject.component.CustomOutlinedTextField
import com.lion.wandertrip.component.BlueButton
import com.lion.wandertrip.R
import com.lion.wandertrip.presentation.bottom.schedule_page.ScheduleScreen
import com.lion.wandertrip.presentation.user_login_page.components.KakaoButton
import com.lion.wandertrip.util.CustomFont
import com.lion.wandertrip.util.MainScreenName
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.forEach

@SuppressLint("RestrictedApi", "ContextCastToActivity")
@Composable
fun UserLoginScreen(userLoginViewModel: UserLoginViewModel = hiltViewModel()) {
    Log.d("UserLoginScreen", "로그인 화면")

    val navController = userLoginViewModel.tripApplication.navHostController
    val backStackEntries = navController.currentBackStack

    val activity = LocalContext.current as? Activity

    LaunchedEffect(activity) {
        if (activity == null) {
            Log.e("UserLoginScreen", "Activity context가 없습니다.")
        } else {
            Log.d("UserLoginScreen", "Activity context 확인됨: ${activity.localClassName}")

          /*  // 여기서 카카오 로그인 로직을 넣거나 안전하게 호출 가능
            userLoginViewModel.getReleaseKeyHash()*/
        }
    }

    LaunchedEffect(Unit) {
        val navController = userLoginViewModel.tripApplication.navHostController

        // 로그인 화면이 열리면 백스택을 다 지우고 로그인 화면만 남기기
        navController.popBackStack(MainScreenName.MAIN_SCREEN_USER_LOGIN.name, false)
    }

    var backStackRoutes by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collectLatest { backStackEntry ->
            // 현재 백스택을 안전하게 가져옴
            val backStackList =
                navController.currentBackStack.value.mapNotNull { it.destination.route }

            backStackRoutes = backStackList // 최신 백스택 반영
        }
    }

    // 백스택 로그 출력
    LaunchedEffect(backStackRoutes) {
        Log.d("BackStack", "===== Current BackStack =====")
        backStackRoutes.forEach { route ->
            Log.d("BackStack", "Route: $route")
        }
        Log.d("BackStack", "=============================")
    }


    val sW = userLoginViewModel.tripApplication.screenWidth
    val sH = userLoginViewModel.tripApplication.screenHeight
    val sR = userLoginViewModel.tripApplication.screenRatio
    Scaffold(
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(it)
                .padding(horizontal = 20.dp), // Row가 화면의 전체 너비를 차지하도록 설정
            horizontalArrangement = Arrangement.Center // Column을 수평 가운데 정렬
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally // 수평 가운데 정렬
            ) {
                // 아이디 입력란 위에 이미지
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_plane),
                        contentDescription = null,
                        modifier = Modifier
                            .heightIn((sH / 6).dp)
                            .fillMaxWidth() // 최소 높이 300dp로 설정
                            .align(Alignment.Center) // 이미지를 중앙에 배치
                            .offset(y = (-(sH / 40)).dp) // 이미지를 위로 50dp만큼 이동시켜서 하단 자르기
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (sH / 50).dp),
                        text = "WanderTrip",
                        fontWeight = FontWeight.Bold, // 진한 글씨로 설정
                        color = Color(0xFF0077B6), // 파란색으로 설정
                        style = TextStyle(
                            fontSize = 35.sp,
                            fontFamily = CustomFont.customFontBold
                        ) // 필요에 따라 글자 크기 설정
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 50.dp)
                ) {
                    // 아이디 입력 요소
                    CustomOutlinedTextField(
                        textFieldValue = userLoginViewModel.textFieldUserLoginIdValue,
                        label = "아이디",
                        placeHolder = "아이디를 입력해주세요",
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_person_24px), // 아이콘
                        trailingIconMode = LikeLionOutlinedTextFieldEndIconMode.TEXT,
                        singleLine = true,
                        focusRequest = userLoginViewModel.textFieldUserLoginIdFocusRequester
                    )


                    // 비밀번호 입력 요소
                    CustomOutlinedTextField(
                        textFieldValue = userLoginViewModel.textFieldUserLoginPasswordValue,
                        label = "비밀번호",
                        placeHolder = "비밀번호를 입력해주세요",
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_key_24px),
                        trailingIconMode = LikeLionOutlinedTextFieldEndIconMode.PASSWORD,
                        singleLine = true,
                        paddingTop = 10.dp,
                        inputType = LikeLionOutlinedTextFieldInputType.PASSWORD,
                        focusRequest = userLoginViewModel.textFieldUserLoginPasswordFocusRequester,
                    )


                    // 자동로그인, 회원가입 버튼
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // CustomCheckBox
                        CustomCheckBox(
                            text = "자동 로그인",
                            checkedValue = userLoginViewModel.checkBoxAutoLoginValue,
                        )

                        // 회원가입 버튼
                        TextButton(
                            onClick = {
                                // 회원가입 버튼 클릭 시 동작
                                userLoginViewModel.buttonUserJoinClick()
                            },
                        ) {
                            Text(text = "회원가입") // "회원가입" 텍스트
                        }
                    }

                }

                BlueButton(
                    buttonWidth = 200.dp,
                    text = "로그인",
                    paddingTop = 5.dp,
                    onClick = {
                        userLoginViewModel.buttonUserLoginOnClick()
                    }
                )

                KakaoButton(
                    paddingTop = 5.dp,
                    onClick = {
                        userLoginViewModel.onClickButtonKakaoLogin(activity!!)
                    }
                )


                // 아이디 입력 오류 다이얼로그
                CustomAlertDialog(
                    showDialogState = userLoginViewModel.alertDialogUserIdState,
                    confirmButtonTitle = "확인",
                    confirmButtonOnClick = {
                        userLoginViewModel.alertDialogUserIdState.value = false
                        userLoginViewModel.textFieldUserLoginIdFocusRequester.value.requestFocus()
                    },
                    title = "아이디 입력 오류",
                    text = "아이디를 입력해주세요"
                )

                // 비밀번호 입력 오류 다이얼로그
                CustomAlertDialog(
                    showDialogState = userLoginViewModel.alertDialogUserPwState,
                    confirmButtonTitle = "확인",
                    confirmButtonOnClick = {
                        userLoginViewModel.alertDialogUserPwState.value = false
                        userLoginViewModel.textFieldUserLoginPasswordFocusRequester.value.requestFocus()
                    },
                    title = "비밀번호 입력 오류",
                    text = "비밀번호를 입력해주세요"
                )

                // 존재하지 않는 아이디
                CustomAlertDialog(
                    showDialogState = userLoginViewModel.alertDialogLoginFail1State,
                    confirmButtonTitle = "확인",
                    confirmButtonOnClick = {
                        userLoginViewModel.textFieldUserLoginIdValue.value = ""
                        userLoginViewModel.textFieldUserLoginPasswordValue.value = ""
                        userLoginViewModel.textFieldUserLoginIdFocusRequester.value.requestFocus()
                        userLoginViewModel.alertDialogLoginFail1State.value = false
                    },
                    title = "로그인 오류",
                    text = "존재하지 않는 아이디 입니다"
                )

                // 잘못된 비밀번호
                CustomAlertDialog(
                    showDialogState = userLoginViewModel.alertDialogLoginFail2State,
                    confirmButtonTitle = "확인",
                    confirmButtonOnClick = {
                        userLoginViewModel.textFieldUserLoginPasswordValue.value = ""
                        userLoginViewModel.textFieldUserLoginPasswordFocusRequester.value.requestFocus()
                        userLoginViewModel.alertDialogLoginFail2State.value = false
                    },
                    title = "로그인 오류",
                    text = "잘못된 비밀번호 입니다"
                )


                // 잘못된 비밀번호
                CustomAlertDialog(
                    showDialogState = userLoginViewModel.alertDialogLoginFail3State,
                    confirmButtonTitle = "확인",
                    confirmButtonOnClick = {
                        userLoginViewModel.textFieldUserLoginIdValue.value = ""
                        userLoginViewModel.textFieldUserLoginPasswordValue.value = ""
                        userLoginViewModel.textFieldUserLoginIdFocusRequester.value.requestFocus()
                        userLoginViewModel.alertDialogLoginFail3State.value = false
                    },
                    title = "로그인 오류",
                    text = "탈퇴한 회원입니다"
                )
            }
        }
    }
}