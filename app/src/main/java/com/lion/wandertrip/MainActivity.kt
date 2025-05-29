package com.lion.wandertrip

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lion.wandertrip.presentation.bottom.trip_note_page.TripNoteScreen
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.lion.wandertrip.presentation.detail_page.DetailScreen
import com.lion.wandertrip.presentation.detail_review_modify_page.DetailReviewModifyScreen
import com.lion.wandertrip.presentation.detail_review_write_page.DetailReviewWriteScreen
import com.lion.wandertrip.presentation.google_map_page.GoogleMapScreen
import com.lion.wandertrip.presentation.trip_note_detail_page.TripNoteDetailScreen
import com.lion.wandertrip.presentation.trip_note_write_page.TripNoteWriteScreen
import com.lion.wandertrip.presentation.main_page.MainScreen
import com.lion.wandertrip.presentation.my_interesting_page.MyInterestingScreen
import com.lion.wandertrip.presentation.my_review_page.MyReviewScreen
import com.lion.wandertrip.presentation.my_trip_note.MyTripNoteScreen
import com.lion.wandertrip.presentation.my_trip_page.MyTripScreen
import com.lion.wandertrip.presentation.popular_city_page.PopularCityScreen
import com.lion.wandertrip.presentation.schedule_add.ScheduleAddScreen
import com.lion.wandertrip.presentation.schedule_city_select.ScheduleCitySelectScreen
import com.lion.wandertrip.presentation.schedule_city_select.city_roulette.RouletteCityScreen
import com.lion.wandertrip.presentation.schedule_city_select.city_roulette.roulette_city_select.RouletteCitySelectScreen
import com.lion.wandertrip.presentation.schedule_detail_friends.ScheduleDetailFriendsScreen
import com.lion.wandertrip.presentation.schedule_detail_page.ScheduleDetailScreen
import com.lion.wandertrip.presentation.schedule_item_review.ScheduleItemReviewScreen
import com.lion.wandertrip.presentation.schedule_select_item.ScheduleSelectItemScreen
import com.lion.wandertrip.presentation.schedule_select_item.roulette_item.RouletteItemScreen
import com.lion.wandertrip.presentation.schedule_select_item.roulette_item.roulette_item_select.RouletteItemSelectScreen
import com.lion.wandertrip.presentation.search_page.SearchScreen
import com.lion.wandertrip.presentation.search_result_page.SearchResultScreen
import com.lion.wandertrip.presentation.start_page.StartScreen
import com.lion.wandertrip.presentation.trip_note_other_schedule_page.TripNoteOtherScheduleScreen
import com.lion.wandertrip.presentation.trip_note_schedule_page.TripNoteScheduleScreen
import com.lion.wandertrip.presentation.trip_note_select_down_page.TripNoteSelectDownScreen
import com.lion.wandertrip.presentation.user_info_page.UserInfoScreen
import com.lion.wandertrip.presentation.user_login_page.UserLoginScreen
import com.lion.wandertrip.presentation.user_sign_up_page.sign_up_step1_page.UserSignUpStep1Screen
import com.lion.wandertrip.presentation.user_sign_up_page.sign_up_step2_page.UserSignUpStep2Screen
import com.lion.wandertrip.presentation.user_sign_up_page.sign_up_step3_page.UserSignUpStep3Screen
import com.lion.wandertrip.ui.theme.WanderTripTheme
import com.lion.wandertrip.util.BotNavScreenName
import com.lion.wandertrip.util.MainScreenName
import com.lion.wandertrip.util.RouletteScreenName
import com.lion.wandertrip.util.ScheduleScreenName
import com.lion.wandertrip.util.TripNoteScreenName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Firebase Auth 익명 로그인
        Firebase.auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity FirebaseAuth", "로그인 성공: ${task.result?.user?.uid}")
                } else {
                    Log.e("MainActivity FirebaseAuth ", "로그인 실패", task.exception)
                }
            }

        setContent {
            WanderTripTheme {
                MyApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp() {
    // 네비게이션 객체
    // Composable 함수가 재구성(recompose)되더라도 NavController의 인스턴스가 유지됨 (즉, 화면 회전이나 재구성이 발생해도 동일한 인스턴스를 사용).
    val rememberNavHostController = rememberNavController()
    // Appilcation 객체에 담는다.
    // Jetpack Compose에서 LocalContext.current는 현재 Composable 함수가 실행되는 컨텍스트(Context)를 가져오는 방법입니다.
    // applicationContext의 기본 타입은 Context이므로, 우리가 만든 TripApplication 클래스의 인스턴스로 사용하려면 형변환이 필요합니다.
    val tripApplication = LocalContext.current.applicationContext as TripApplication
    tripApplication.navHostController = rememberNavHostController

    NavHost(
        navController = rememberNavHostController,
        startDestination = MainScreenName.MAIN_SCREEN_START.name
    ) {
        composable(MainScreenName.MAIN_SCREEN_START.name) { StartScreen() }
        // 일정 메인 화면
        composable(MainScreenName.MAIN_SCREEN_USER_LOGIN.name) { UserLoginScreen() }
        composable(BotNavScreenName.BOT_NAV_SCREEN_HOME.name) { MainScreen() }
        composable(MainScreenName.MAIN_SCREEN_USER_SIGN_UP_STEP1.name) { UserSignUpStep1Screen() }
        composable(
            route = "${MainScreenName.MAIN_SCREEN_USER_SIGN_UP_STEP2.name}/{fromWhere}/{userDocId}"
        ) { backStackEntry ->
            val fromWhere = backStackEntry.arguments?.getString("fromWhere") ?: ""
            val userDocId = backStackEntry.arguments?.getString("userDocId") ?: ""
            UserSignUpStep2Screen(userDocId = userDocId, fromWhere = fromWhere)
        }
        composable(MainScreenName.MAIN_SCREEN_USER_SIGN_UP_STEP3.name) { UserSignUpStep1Screen() }

        // 카카오 가입
        composable("${MainScreenName.MAIN_SCREEN_USER_SIGN_UP_STEP3.name}/{kakaoToken}") { backStackEntry ->
            val kakaoToken: String = backStackEntry.arguments?.getString("kakaoToken") ?: ""
            UserSignUpStep3Screen(kakaoToken = kakaoToken)
        }

        composable(MainScreenName.MAIN_SCREEN_SEARCH.name) { SearchScreen() }
        composable(MainScreenName.MAIN_SCREEN_USER_INFO.name) { UserInfoScreen() }

        composable("${MainScreenName.MAIN_SCREEN_SEARCH_RESULT.name}/{contentId}") { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: "default_id"
            SearchResultScreen(contentId)
        }

//        // 여행기 메인 화면
//        composable(
//            route = "${BotNavScreenName.BOT_NAV_SCREEN_TRIP_NOTE.name}/{documentId}"
//        ){
//            val documentId = it.arguments?.getString("documentId") ?:  ""
//            TripNoteScreen(documentId = documentId)
//        }

        composable(BotNavScreenName.BOT_NAV_SCREEN_TRIP_NOTE.name) { TripNoteScreen() }

        // 여행기 상세 화면
        composable(
            route = "${TripNoteScreenName.TRIP_NOTE_DETAIL.name}/{documentId}"
        ) {
            val documentId = it.arguments?.getString("documentId") ?: ""
            TripNoteDetailScreen(tripNoteDocumentId = documentId)
        }


        // 여행기 작성 화면
        composable(
            route = "${TripNoteScreenName.TRIP_NOTE_WRITE.name}/{scheduleTitle}/{scheduleDocId}"
        ) {
            val scheduleTitle = it.arguments?.getString("scheduleTitle") ?: ""
            val scheduleDocId = it.arguments?.getString("scheduleDocId") ?: ""
            TripNoteWriteScreen(tripScheduleTitle = scheduleTitle, scheduleDocId = scheduleDocId)
        }

        // 여행기 페이지에서 다른 사람 여행기 보기
        composable(
            route = "${TripNoteScreenName.TRIP_NOTE_OTHER_SCHEDULE.name}/{otherNickName}"
        ) {
            val otherNickName = it.arguments?.getString("otherNickName") ?: ""
            TripNoteOtherScheduleScreen(otherNickName = otherNickName)
        }

        // 여행기에서 다른 사람 일정 다운
        // composable(TripNoteScreenName.TRIP_NOTE_SELECT_DOWN.name) { TripNoteSelectDownScreen() }
        composable(
            route = "${TripNoteScreenName.TRIP_NOTE_SELECT_DOWN.name}/{tripNoteScheduleDocId}/{documentId}"
        ) {
            val tripNoteScheduleDocId = it.arguments?.getString("tripNoteScheduleDocId") ?: ""
            val documentId = it.arguments?.getString("documentId") ?: ""

            TripNoteSelectDownScreen(
                tripNoteScheduleDocId = tripNoteScheduleDocId,
                documentId = documentId
            )
        }


        // 여행기에서 일정 가져오기
        composable(TripNoteScreenName.TRIP_NOTE_SCHEDULE.name) { TripNoteScheduleScreen() }


        // 일정 화면 ////////////////////////////////////////////////////////////////////////////

        // 일정 제목, 날짜 입력 화면
        // composable(ScheduleScreenName.SCHEDULE_ADD_SCREEN.name) { ScheduleAddScreen() }
        composable(
            route = "${ScheduleScreenName.SCHEDULE_ADD_SCREEN.name}/{documentId}"
        ) {
            val documentId = it.arguments?.getString("documentId") ?: ""

            ScheduleAddScreen(documentId = documentId)
        }


        // 내 여행 화면
        composable(MainScreenName.MAIN_SCREEN_MY_TRIP.name) { MyTripScreen() }
        // 내 저장 화면
        composable(MainScreenName.MAIN_SCREEN_MY_INTERESTING.name) { MyInterestingScreen() }
        // 내 리뷰 화면
        composable(MainScreenName.MAIN_SCREEN_MY_REVIEW.name) { MyReviewScreen() }
        // 내 여행기 화면
        composable(MainScreenName.MAIN_SCREEN_MY_TRIP_NOTE.name) { MyTripNoteScreen() }

        // 상세 화면
        // ✅ contentId를 받도록 설정
        composable("${MainScreenName.MAIN_SCREEN_DETAIL.name}/{contentId}") { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: "default_id"
            DetailScreen(contentId)
        }

        // 구글 맵 화면
        composable("${MainScreenName.MAIN_SCREEN_GOOGLE_MAP.name}/{contentId}") { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: "default_id"
            GoogleMapScreen(contentId)
        }


        // 일정 도시 선택 화면
        composable(
            route = "${ScheduleScreenName.SCHEDULE_CITY_SELECT_SCREEN.name}?" +
                    "scheduleTitle={scheduleTitle}&scheduleStartDate={scheduleStartDate}&scheduleEndDate={scheduleEndDate}",
            arguments = listOf(
                navArgument("scheduleTitle") { type = NavType.StringType },
                navArgument("scheduleStartDate") { type = NavType.LongType },
                navArgument("scheduleEndDate") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val scheduleTitle = backStackEntry.arguments?.getString("scheduleTitle") ?: ""
            val startTimestamp = backStackEntry.arguments?.getLong("scheduleStartDate") ?: 0L
            val endTimestamp = backStackEntry.arguments?.getLong("scheduleEndDate") ?: 0L

            val startDate = Timestamp(startTimestamp, 0)
            val endDate = Timestamp(endTimestamp, 0)

            ScheduleCitySelectScreen(scheduleTitle, startDate, endDate)
        }

        // 일정 상세 화면
        composable(
            route = "${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
                    "tripScheduleDocId={tripScheduleDocId}&areaName={areaName}&areaCode={areaCode}",
            arguments = listOf(
                navArgument("tripScheduleDocId") { type = NavType.StringType },
                navArgument("areaName") { type = NavType.StringType },
                navArgument("areaCode") { type = NavType.IntType }
            )
        ) {
            val tripScheduleDocId = it.arguments?.getString("tripScheduleDocId") ?: ""
            val areaName = it.arguments?.getString("areaName") ?: ""
            val areaCode = it.arguments?.getInt("areaCode") ?: 0
            ScheduleDetailScreen(tripScheduleDocId, areaName, areaCode)
        }

        // 일정 항목 선택 화면
        composable(
            route = "${ScheduleScreenName.SCHEDULE_SELECT_ITEM_SCREEN.name}?" +
                    "itemCode={itemCode}&areaName={areaName}&areaCode={areaCode}&scheduleDate={scheduleDate}&tripScheduleDocId={tripScheduleDocId}",
            arguments = listOf(
                navArgument("itemCode") { type = NavType.IntType },
                navArgument("areaName") { type = NavType.StringType },
                navArgument("areaCode") { type = NavType.IntType },
                navArgument("scheduleDate") { type = NavType.LongType },
                navArgument("tripScheduleDocId") { type = NavType.StringType },
            )
        ) {
            val areaName = it.arguments?.getString("areaName") ?: ""
            val areaCode = it.arguments?.getInt("areaCode") ?: 0
            val itemCode = it.arguments?.getInt("itemCode") ?: 0
            val scheduleDate = it.arguments?.getLong("scheduleDate") ?: 0
            val tripScheduleDocId = it.arguments?.getString("tripScheduleDocId") ?: ""
            ScheduleSelectItemScreen(itemCode, areaName, areaCode, scheduleDate, tripScheduleDocId)
        }

        // 일정 초대한 친구 목록 화면
        composable(
            route = "${ScheduleScreenName.SCHEDULE_DETAIL_FRIENDS_SCREEN.name}?" +
                    "scheduleDocId={scheduleDocId}&userNickName={userNickName}",
            arguments = listOf(
                navArgument("scheduleDocId") { type = NavType.StringType },
                navArgument("userNickName") { type = NavType.StringType }
            )
        ) {
            val scheduleDocId = it.arguments?.getString("scheduleDocId") ?: ""
            val userNickName = it.arguments?.getString("userNickName") ?: ""
            ScheduleDetailFriendsScreen(scheduleDocId, userNickName)
        }

        composable(
            route = ScheduleScreenName.SCHEDULE_ITEM_REVIEW_SCREEN.name +
                    "/{tripScheduleDocId}/{scheduleItemDocId}/{scheduleItemTitle}",
            arguments = listOf(
                navArgument("tripScheduleDocId") { type = NavType.StringType },
                navArgument("scheduleItemDocId") { type = NavType.StringType },
                navArgument("scheduleItemTitle") { type = NavType.StringType },
            )
        ) {
            val tripScheduleDocId = it.arguments?.getString("tripScheduleDocId") ?: ""
            val scheduleItemDocId = it.arguments?.getString("scheduleItemDocId") ?: ""
            val scheduleItemTitle = it.arguments?.getString("scheduleItemTitle") ?: ""
            ScheduleItemReviewScreen(tripScheduleDocId, scheduleItemDocId, scheduleItemTitle)
        }

        /////////////////////////////////////////////////////////////////////////////////////////////


        // 룰렛 화면 /////////////////////////////////////////////////////////////////////////////////

        // 도시 룰렛 메인 화면
        composable(
            route = "${RouletteScreenName.ROULETTE_CITY_SCREEN.name}?" +
                    "scheduleTitle={scheduleTitle}&scheduleStartDate={scheduleStartDate}&scheduleEndDate={scheduleEndDate}",
            arguments = listOf(
                navArgument("scheduleTitle") { type = NavType.StringType },
                navArgument("scheduleStartDate") { type = NavType.LongType },
                navArgument("scheduleEndDate") { type = NavType.LongType }
            )
        ) {
            val scheduleTitle = it.arguments?.getString("scheduleTitle") ?: ""
            val startTimestamp = it.arguments?.getLong("scheduleStartDate") ?: 0L
            val endTimestamp = it.arguments?.getLong("scheduleEndDate") ?: 0L

            val startDate = Timestamp(startTimestamp, 0)
            val endDate = Timestamp(endTimestamp, 0)

            RouletteCityScreen(scheduleTitle, startDate, endDate)
        }

        // 룰렛 도시 항목 추가 화면
        composable(RouletteScreenName.ROULETTE_CITY_SELECT_SCREEN.name) {
            RouletteCitySelectScreen(
                navController = rememberNavHostController
            )
        }

        // 룰렛 일정 화면
        composable(
            route = "${RouletteScreenName.ROULETTE_ITEM_SCREEN.name}?" +
                    "tripScheduleDocId={tripScheduleDocId}&areaName={areaName}&areaCode={areaCode}&scheduleDate={scheduleDate}",
            arguments = listOf(
                navArgument("tripScheduleDocId") { type = NavType.StringType },
                navArgument("areaName") { type = NavType.StringType },
                navArgument("areaCode") { type = NavType.LongType },
                navArgument("scheduleDate") { type = NavType.LongType }
            )
        ) {
            val tripScheduleDocId = it.arguments?.getString("tripScheduleDocId") ?: ""
            val areaName = it.arguments?.getString("areaName") ?: ""
            val areaCode = it.arguments?.getInt("areaCode") ?: 0
            val scheduleDateTemp = it.arguments?.getLong("scheduleDate") ?: 0L
            val scheduleDate = Timestamp(scheduleDateTemp, 0)

            RouletteItemScreen(tripScheduleDocId, areaName, areaCode, scheduleDate)
        }

        // 룰렛 일정 항목 선택 화면
        composable(RouletteScreenName.ROULETTE_ITEM_SELECT_SCREEN.name) {
            RouletteItemSelectScreen(
                navController = rememberNavHostController
            )
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // 리뷰 작성 화면
        composable(
            route = "${MainScreenName.MAIN_SCREEN_DETAIL_REVIEW_WRITE.name}/{contentID}/{contentTitle}",
            arguments = listOf(
                navArgument("contentID") { type = NavType.StringType },
                navArgument("contentTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contentID = backStackEntry.arguments?.getString("contentID") ?: ""
            val contentTitle = backStackEntry.arguments?.getString("contentTitle") ?: ""

            DetailReviewWriteScreen(contentID = contentID, contentTitle = contentTitle)
        }
        // 리뷰 수정 화면
        composable(
            route = "${MainScreenName.MAIN_SCREEN_DETAIL_REVIEW_MODIFY.name}/{contentDocID}/{contentsID}/{reviewDocID}",
            arguments = listOf(
                navArgument("contentDocID") { type = NavType.StringType },
                navArgument("contentsID") { type = NavType.StringType },
                navArgument("reviewDocID") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contentDocID = backStackEntry.arguments?.getString("contentDocID") ?: ""
            val contentsID = backStackEntry.arguments?.getString("contentsID") ?: ""
            val reviewDocID = backStackEntry.arguments?.getString("reviewDocID") ?: ""

            DetailReviewModifyScreen(
                contentDocID = contentDocID,
                contentsID = contentsID,
                reviewDocID = reviewDocID
            )
        }

        // 인기 도시 화면
        composable(
            route = "${
                MainScreenName.MAIN_SCREEN_POPULAR_CITY
                    .name
            }/{lat}/{lng}/{cityName}/{radius}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType },
                navArgument("cityName") { type = NavType.StringType },
                navArgument("radius") { type = NavType.StringType },

                )
        ) { backStackEntry ->
         /*   val lat = backStackEntry.arguments?.getString("lat") ?: ""
            val lng = backStackEntry.arguments?.getString("lng") ?: ""
            val radius = backStackEntry.arguments?.getString("radius") ?: ""*/
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""


            PopularCityScreen(/*lat, lng, */cityName,/*radius*/)
        }


    }
}



