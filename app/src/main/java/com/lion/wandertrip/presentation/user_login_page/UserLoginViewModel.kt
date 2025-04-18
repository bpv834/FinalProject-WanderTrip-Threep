package com.lion.wandertrip.presentation.user_login_page

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.service.UserService
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.MainScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi
import android.util.Base64
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.lion.wandertrip.util.LoginResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class UserLoginViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val userService: UserService,
) : ViewModel() {

    val tripApplication = context as TripApplication

    // 아이디 입력 요소
    val textFieldUserLoginIdValue = mutableStateOf("")

    // 비밀번호 입력 요소
    val textFieldUserLoginPasswordValue = mutableStateOf("")

    // 자동 로그인 입력 요소
    val checkBoxAutoLoginValue = mutableStateOf(false)

    // 아이디 입력요소 포커스
    val textFieldUserLoginIdFocusRequester = mutableStateOf(FocusRequester())

    // 비밀번호 입력 요소 포커스
    val textFieldUserLoginPasswordFocusRequester = mutableStateOf(FocusRequester())

    // 아이디 입력 오류 다이얼로그 상태변수
    val alertDialogUserIdState = mutableStateOf(false)

    // 비밀번호 입력 오류 다이얼로그 상태변수
    val alertDialogUserPwState = mutableStateOf(false)

    // 존재하지 않는 아이디 오류 다이얼로그 상태변수
    val alertDialogLoginFail1State = mutableStateOf(false)

    // 잘못된 비밀번호 다이얼로그 상태변수
    val alertDialogLoginFail2State = mutableStateOf(false)

    // 탈퇴한 회원 다이얼로그 상태변수
    val alertDialogLoginFail3State = mutableStateOf(false)


    // 회원 가입 버튼 click
    fun buttonUserJoinClick() {
        tripApplication.navHostController.navigate(MainScreenName.MAIN_SCREEN_USER_SIGN_UP_STEP1.name)
    }

    // 로그인 버튼 click
    fun buttonUserLoginOnClick() {
        Log.d("test100", "클릭")

        if (textFieldUserLoginIdValue.value.isEmpty()) {
            alertDialogUserIdState.value = true
            return
        }

        if (textFieldUserLoginPasswordValue.value.isEmpty()) {
            alertDialogUserPwState.value = true
            return
        }

        // 사용자가 입력한 아이디와 비밀번호
        val loginUserId = textFieldUserLoginIdValue.value
        val loginUserPw = textFieldUserLoginPasswordValue.value // 암호화하지 않고 원본 비밀번호를 받음

        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO) {
                userService.selectUserDataByUserIdOne(loginUserId)
            }
            // 로그인한 사용자 데이터를 가져옴
            val loginUserModel = work1.await()

            // 가져온 암호화된 비밀번호와 입력된 비밀번호를 비교
            if (BCrypt.checkpw(loginUserPw, loginUserModel.userPw)) { // 입력된 비밀번호와 저장된 암호화된 비밀번호 비교
                // 로그인 성공시
                if (checkBoxAutoLoginValue.value) {
                    // 자동 로그인 처리
                    CoroutineScope(Dispatchers.Main).launch {
                        val work3 = async(Dispatchers.IO) {
                            userService.updateUserAutoLoginToken(
                                tripApplication,
                                loginUserModel.userDocId
                            )
                        }
                        work3.join()
                    }
                }

                // Application 객체에 로그인한 사용자의 정보를 담고 게시판 메인 화면으로 이동한다.
                tripApplication.loginUserModel = loginUserModel

                tripApplication.navHostController.navigate(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) {
                    // 홈 화면은 남기고 그 이전의 화면들만 백스택에서 제거
                    popUpTo(MainScreenName.MAIN_SCREEN_USER_LOGIN.name) { inclusive = true }
                }
            } else {
                // 비밀번호 불일치
                alertDialogLoginFail2State.value = true
            }
        }
    }

    // 카카오 로그인 버튼 눌렀을 때
    fun onClickButtonKakaoLogin(context: Context) {
        /*val context = tripApplication*/

        viewModelScope.launch {
            var str: String? = "isError"

            val work1 = async(Dispatchers.IO) {
                try {
                    str = createKakaoToken(context)
                    Log.d("onClickButtonKakaoLogin","str: $str")

                } catch (e: Exception) {
                    Log.e("KakaoLogin", "카카오 토큰 생성 실패: ${e.localizedMessage}")
                    Toast.makeText(context, "카카오 토큰 생성 실패", Toast.LENGTH_SHORT).show()
                }
            }
            work1.join()

            val kToken = try {
                Log.d("onClickButtonKakaoLogin","2222222")
                work1.await()


            } catch (e: Exception) {
                Log.e("KakaoLogin", "카카오 토큰 await 실패: ${e.localizedMessage}")
                Toast.makeText(context, "카카오 로그인 중 오류 발생", Toast.LENGTH_SHORT).show()
                null
            }

            if (str == null || kToken == null) {
                Toast.makeText(context, "카카오 로그인 실패 ${str?:"str이 널"} ${kToken?:"kToken이 널"}!!!!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val work2 = async(Dispatchers.IO) {
                try {
                    getKakaoUserId()
                } catch (e: Exception) {
                    Log.e("KakaoLogin", "카카오 사용자 ID 가져오기 실패: ${e.localizedMessage}")
                    Toast.makeText(context, "카카오 사용자 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
                    null
                }
            }
            work2.join()

            val kakaoId = try {
                work2.await()
            } catch (e: Exception) {
                Log.e("KakaoLogin", "카카오 사용자 ID await 실패: ${e.localizedMessage}")
                Toast.makeText(context, "카카오 ID 처리 실패", Toast.LENGTH_SHORT).show()
                null
            }

            if (kakaoId == null) {
                Toast.makeText(context, "카카오 사용자 ID가 유효하지 않습니다", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val model = try {
                userService.selectUserDataByKakaoLoginToken(kakaoId)
            } catch (e: Exception) {
                Log.e("KakaoLogin", "서버에서 사용자 정보 조회 실패: ${e.localizedMessage}")
                Toast.makeText(context, "서버 오류: 사용자 정보 조회 실패", Toast.LENGTH_SHORT).show()
                null
            }

            if (model != null) {
                tripApplication.loginUserModel = model

                tripApplication.navHostController.navigate(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) {
                    popUpTo(MainScreenName.MAIN_SCREEN_USER_LOGIN.name) { inclusive = true }
                }

                try {
                    val pref = tripApplication.getSharedPreferences("KakaoToken", Context.MODE_PRIVATE)
                    pref.edit {
                        putString("kToken", model.kakaoId.toString())
                        Log.d("userSingStep3", "ktoken: ${model.kakaoId}")
                    }

                    val kakaoPref = tripApplication.getSharedPreferences("KakaoToken", Context.MODE_PRIVATE)
                    val ktToken = kakaoPref.getString("kToken", null)
                    Log.d("userSingStep3", "토큰 가져오기 : $ktToken")
                } catch (e: Exception) {
                    Log.e("KakaoLogin", "SharedPreferences 처리 실패: ${e.localizedMessage}")
                    Toast.makeText(context, "토큰 저장 실패", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(context, "등록되지 않은 사용자입니다", Toast.LENGTH_SHORT).show()
                if (kToken != null) {
                    tripApplication.navHostController.navigate("${MainScreenName.MAIN_SCREEN_USER_SIGN_UP_STEP3.name}/${kakaoId}")
                }
            }
        }
    }




    // 카카오 로그인 토큰 받아오기
    suspend fun createKakaoToken(context: Context): String? = suspendCoroutine { continuation ->
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("test100", "카카오계정으로 로그인 실패", error)
                continuation.resume(null) // 실패 시 null 반환
            } else if (token != null) {
                Log.i("test100", "카카오계정으로 로그인 성공 ${token.accessToken}")
                continuation.resume(token.accessToken) // 성공 시 토큰 반환
            }
        }

        // `context`에 Activity를 명시적으로 넘기기
        val activityContext = context

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activityContext)) {
            Log.d("createKakaoToken", "카카오톡이 단말기에 깔려있음")
            UserApiClient.instance.loginWithKakaoTalk(activityContext) { token, error ->
                if (error != null) {
                    Log.e("test100", "카카오톡으로 로그인 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resume(null) // 로그인 취소 시 null 반환
                        return@loginWithKakaoTalk
                    }

                    // 카카오 계정 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(
                        activityContext,
                        callback = callback
                    )
                } else if (token != null) {
                    Log.i("test100", "카카오톡으로 로그인 성공 ${token.accessToken}")
                    continuation.resume(token.accessToken) // 성공 시 토큰 반환
                }
            }
        } else {
            Log.d("createKakaoToken", "카카오톡이 단말기에 없음")
            UserApiClient.instance.loginWithKakaoAccount(activityContext, callback = callback)
        }
    }

    // 카카오 ID를 가져오는 함수
    suspend fun getKakaoUserId(): Long? = suspendCoroutine { continuation ->
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("KakaoUserInfo", "사용자 정보 요청 실패", error)
                continuation.resume(null) // 실패 시 null 반환
            } else if (user != null) {
                val kakaoId = user.id // 카카오 계정 ID
                Log.i("KakaoUserInfo", "카카오 ID: $kakaoId")
                continuation.resume(kakaoId) // 카카오 ID 반환
            }
        }
    }




    // 디버그 키해시 받아오는 메서드
    @OptIn(ExperimentalEncodingApi::class)
    private fun getHashKey() {
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = tripApplication.packageManager.getPackageInfo(
                tripApplication.packageName,
                PackageManager.GET_SIGNATURES
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (packageInfo == null) {
            Log.e("KeyHash", "KeyHash:null")
            return
        }

        for (signature in packageInfo.signatures!!) {
            try {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))  // 수정된 부분
            } catch (e: NoSuchAlgorithmException) {
                Log.d("test100", "Unable to get MessageDigest. signature=$signature")
            }
        }
    }

    // 릴리즈 해쉬키 받기
    @OptIn(ExperimentalEncodingApi::class)
    private fun getReleaseKeyHash() {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                tripApplication.packageManager.getPackageInfo(
                    tripApplication.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                tripApplication.packageManager.getPackageInfo(
                    tripApplication.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures != null) {
                for (signature in signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                    Log.d("KeyHash", "릴리즈 키 해시: $keyHash")
                }
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "키 해시 구하는 중 오류 발생", e)
        }
    }

}