package com.lion.wandertrip.presentation.user_sign_up_page.sign_up_step1_page

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lion.a02_boardcloneproject.component.CustomOutlinedTextField
import com.lion.a02_boardcloneproject.component.CustomTopAppBar
import com.lion.a02_boardcloneproject.component.LikeLionOutlinedTextFieldEndIconMode
import com.lion.a02_boardcloneproject.component.LikeLionOutlinedTextFieldInputType
import com.lion.wandertrip.R
import com.lion.wandertrip.component.BlueButton

@Composable
fun UserSignUpStep1Screen(userSignUpStep1ViewModel: UserSignUpStep1ViewModel = hiltViewModel()) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CustomTopAppBar(
                title = "회원 가입",
                navigationIconImage = Icons.AutoMirrored.Filled.ArrowBack,
                navigationIconOnClick = {
                    userSignUpStep1ViewModel.navigationIconOnClick()
                },
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(start = 30.dp, end = 30.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Column(
            ) {

                // 아이디 입력 요소
                CustomOutlinedTextField(
                    textFieldValue = userSignUpStep1ViewModel.textFieldUserJoinStep1IdValue,
                    label = "아이디",
                    placeHolder = "4~20자 영문,숫자로 구성",
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_person_24px),
                    singleLine = true,
                    supportText = userSignUpStep1ViewModel.textFieldUserJoinStep1IdErrorText,
                    isError = userSignUpStep1ViewModel.textFieldUserJoinStep1IdIsError,
                    isCheckValue = userSignUpStep1ViewModel.isCheckUserId,
                    readOnly = userSignUpStep1ViewModel.textFieldUserJoinStep1IdReadOnly.value

                    )

                // 중복 확인 버튼
                BlueButton(
                    text = "아이디 중복 확인",
                    paddingTop = 10.dp,
                    onClick = {
                        userSignUpStep1ViewModel.onClickButtonCheckUserId()
                    }
                )

                // 비밀번호 입력 1 요소
                CustomOutlinedTextField(
                    textFieldValue = userSignUpStep1ViewModel.textFieldUserJoinStep1Password1Value,
                    label = "비밀번호",
                    placeHolder = "8자 이상 소문자,숫자,특수문자 구성",
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_key_24px),
                    trailingIconMode = LikeLionOutlinedTextFieldEndIconMode.PASSWORD,
                    singleLine = true,
                    paddingTop = 10.dp,
                    inputType = LikeLionOutlinedTextFieldInputType.PASSWORD,
                    supportText = userSignUpStep1ViewModel.textFieldUserJoinStep1Password1ErrorText,
                    isError = userSignUpStep1ViewModel.textFieldUserJoinStep1Password1IsError,
                )

                // 비밀번호 입력 2 요소
                CustomOutlinedTextField(
                    textFieldValue = userSignUpStep1ViewModel.textFieldUserJoinStep1Password2Value,
                    label = "비밀번호 확인",
                    placeHolder = "비밀번호를 입력해주세요",
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_key_24px),
                    trailingIconMode = LikeLionOutlinedTextFieldEndIconMode.PASSWORD,
                    singleLine = true,
                    paddingTop = 10.dp,
                    inputType = LikeLionOutlinedTextFieldInputType.PASSWORD,
                    supportText = userSignUpStep1ViewModel.textFieldUserJoinStep1Password2ErrorText,
                    isError = userSignUpStep1ViewModel.textFieldUserJoinStep1Password2IsError,
                )

                // 닉네임 요소
                CustomOutlinedTextField(
                    textFieldValue = userSignUpStep1ViewModel.textFieldUserJoinStep1NickNameValue,
                    label = "닉네임",
                    placeHolder = "닉네임을 입력해주세요",
                    singleLine = true,
                    paddingTop = 10.dp,
                    inputType = LikeLionOutlinedTextFieldInputType.TEXT,
                    supportText = userSignUpStep1ViewModel.textFieldUserJoinStep1NickNameErrorText,
                    isError = userSignUpStep1ViewModel.textFieldUserJoinStep1NickNameIsError,
                    readOnly = userSignUpStep1ViewModel.textFieldUserJoinStep1NickNameReadOnly.value
                )


                // 닉네임 중복 확인 버튼
                BlueButton(
                    text = "닉네임 중복 확인",
                    paddingTop = 10.dp,
                    onClick = {
                        userSignUpStep1ViewModel.onClickButtonCheckUserNickName()
                    }
                )
            }



            // 회원가입 완료
            BlueButton(
                text = "회원가입 완료",
                paddingTop = 10.dp,
                onClick = {
                    userSignUpStep1ViewModel.onClickButtonSignUp()
                }
            )
        }
    }
}