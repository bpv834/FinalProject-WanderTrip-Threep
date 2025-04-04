package com.lion.wandertrip.presentation.schedule_add

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.util.AreaCode
import com.lion.wandertrip.util.ScheduleScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ScheduleAddViewModel @Inject constructor(
    @ApplicationContext val context: Context,
) : ViewModel() {

    val application = context as TripApplication

    // 일정 제목
    val scheduleTitle = mutableStateOf("")
    // 일정 시작 날짜
    val scheduleStartDate = mutableStateOf<Timestamp>(Timestamp.now())
    // 일정 종료 날짜
    val scheduleEndDate = mutableStateOf<Timestamp>(Timestamp.now())

    var newScheduleDocId = ""
    var areaCode : Int = 0


    // ✅ Timestamp -> "YYYY.MM.DD" 형식 변환 함수
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTimestampToDateString(timestamp: Timestamp): String {
        val localDate = Instant.ofEpochMilli(timestamp.seconds * 1000)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd") // ✅ 년-월-일 포맷 적용
        return localDate.format(formatter)
    }

    // 도시 선택 화면 이동 리스너
    fun moveToScheduleCitySelectScreen() {
        val formattedTitle = scheduleTitle.value
        val startTimestamp = scheduleStartDate.value.seconds // 🔹 Timestamp -> Long 변환
        val endTimestamp = scheduleEndDate.value.seconds // 🔹 Timestamp -> Long 변환

        application.navHostController.navigate(
            "${ScheduleScreenName.SCHEDULE_CITY_SELECT_SCREEN.name}?" +
                    "scheduleTitle=$formattedTitle" +
                    "&scheduleStartDate=$startTimestamp" +
                    "&scheduleEndDate=$endTimestamp"
        )
    }




    // 날짜 범위 사이의 모든 날짜를 타임스탬프 리스트로 반환하는 함수
    fun generateDateRangeList(startDate: Timestamp, endDate: Timestamp): List<Timestamp> {
        val dateList = mutableListOf<Timestamp>()

        // Calendar 객체를 사용해 날짜를 하루씩 증가시키며 리스트에 추가
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate.seconds * 1000 // Timestamp의 seconds를 밀리초로 변환

        while (calendar.timeInMillis <= endDate.seconds * 1000) {
            dateList.add(Timestamp(calendar.timeInMillis / 1000, 0)) // Timestamp로 추가
            calendar.add(Calendar.DAY_OF_MONTH, 1) // 하루를 더함
        }

        return dateList
    }



//    fun goScheduleTitleButtonClick(
//        tripNoteScheduleDocId: String
//    ) {
//        val firestore = FirebaseFirestore.getInstance()
//        val tripNoteScheduleRef = firestore.collection("TripSchedule").document(tripNoteScheduleDocId)
//
//        tripNoteScheduleRef.get().addOnSuccessListener { originalScheduleDoc ->
//            if (!originalScheduleDoc.exists()) {
//                Log.e("Firestore", "❌ 원본 일정 문서를 찾을 수 없음.")
//                return@addOnSuccessListener
//            }
//
//            // 원본 일정에서 scheduleCity와 날짜 값 가져오기
//            val scheduleCity = originalScheduleDoc.getString("scheduleCity") ?: return@addOnSuccessListener
//            val originalStartDate = (originalScheduleDoc.get("scheduleStartDate") as? Timestamp)?.seconds ?: return@addOnSuccessListener
//            val originalEndDate = (originalScheduleDoc.get("scheduleEndDate") as? Timestamp)?.seconds ?: return@addOnSuccessListener
//
//            val scheduleDateList = generateDateRangeList(scheduleStartDate.value, scheduleEndDate.value)
//
//            // scheduleCity 값에 해당하는 areaCode를 찾는 방법
//            areaCode = AreaCode.values().find { it.areaName == scheduleCity }?.areaCode!!
//
//            // 새 문서 데이터 생성
//            val newScheduleDocData = hashMapOf(
//                "scheduleCity" to scheduleCity,
//                "scheduleStartDate" to scheduleStartDate.value,
//                "scheduleEndDate" to scheduleEndDate.value,
//                "scheduleDateList" to scheduleDateList,
//                "scheduleState" to 1,
//                "scheduleTimeStamp" to Timestamp.now(),
//                "scheduleTitle" to scheduleTitle.value,
//                "userID" to application.loginUserModel.userId,
//                "userNickName" to application.loginUserModel.userNickName,
//                "scheduleInviteList" to listOf(application.loginUserModel.userDocId),
//                "scheduleItems" to emptyList<Any>()
//            )
//
//            // TripSchedule 컬렉션에 새 문서 추가
//            firestore.collection("TripSchedule").add(newScheduleDocData)
//                .addOnSuccessListener { newScheduleDoc ->
//                    val newScheduleRef = firestore.collection("TripSchedule").document(newScheduleDoc.id)
//                    newScheduleRef.update("tripScheduleDocId", newScheduleDoc.id)
//                        .addOnSuccessListener {
//                            Log.d("Firestore", "✅ 새 일정 문서 생성 완료: ${newScheduleDoc.id}")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("Firestore", "❌ tripScheduleDocId 업데이트 실패", e)
//                        }
//
//                    newScheduleDocId = newScheduleDoc.id
//                    Log.d("Firestore", "✅ 새 일정 문서 ID: $newScheduleDocId")
//
//                    // 기존 일정의 아이템들 날짜 조정 작업
//                    tripNoteScheduleRef.collection("TripScheduleItem").get().addOnSuccessListener { snapshot ->
//                        val adjustedItems = snapshot.documents
//                            .mapNotNull { it.data?.toMutableMap() }
//                            .map { item ->
//                                val itemDate = (item["itemDate"] as? Timestamp)?.seconds ?: return@map null
//
//                                // 날짜 변환: 원본 일정의 itemDate를 새 일정에 맞게 변환
//                                val adjustedItemDate = getAdjustedItemDate(
//                                    originalStartDate,
//                                    originalEndDate,
//                                    itemDate,
//                                    scheduleStartDate.value.seconds,
//                                    scheduleEndDate.value.seconds
//                                )
//
//                                // 날짜만 추출
//                                val adjustedItemDateOnly = getDateOnly(adjustedItemDate)
//
//                                // 변환된 날짜 추가
//                                item["itemDate"] = Timestamp(adjustedItemDateOnly, 0)
//
//                                item // 변환된 아이템 반환
//                            }
//                            .filterNotNull()
//
//                        // 날짜 기준으로 그룹화
//                        val groupedItems = adjustedItems.groupBy { (it["itemDate"] as? Timestamp)?.seconds }
//
//                        // 각 그룹에 대해 인덱스 부여
//                        groupedItems.forEach { (itemDate, items) ->
//                            items.forEachIndexed { index, item ->
//                                // itemIndex를 1부터 시작하여 부여
//                                item["itemIndex"] = index + 1
//
//                                // 새 일정의 TripScheduleItem에 아이템 추가
//                                newScheduleRef.collection("TripScheduleItem").add(item)
//                                    .addOnSuccessListener { newItemDoc ->
//                                        newItemDoc.update("itemDocId", newItemDoc.id)
//                                            .addOnSuccessListener {
//                                                Log.d("Firestore", "✅ 아이템 문서 ID 업데이트 완료: ${newItemDoc.id}")
//                                            }
//                                            .addOnFailureListener { e ->
//                                                Log.e("Firestore", "❌ 아이템 문서 ID 업데이트 실패", e)
//                                            }
//                                        Log.d("Firestore", "✅ 새 일정 아이템 추가 완료: ${newItemDoc.id}")
//                                    }
//                                    .addOnFailureListener { e ->
//                                        Log.e("Firestore", "❌ 새 일정 아이템 추가 실패", e)
//                                    }
//                            }
//                        }
//                    }
//
//                    // ✅ UserData 컬렉션에서 userDocId에 해당하는 문서를 찾아 userScheduleList 업데이트
//                    val userDocRef = firestore.collection("UserData").document(application.loginUserModel.userDocId)
//                    userDocRef.get().addOnSuccessListener { userDoc ->
//                        if (userDoc.exists()) {
//                            val userScheduleList = (userDoc.get("userScheduleList") as? MutableList<String>) ?: mutableListOf()
//                            userScheduleList.add(newScheduleDocId)
//
//                            userDocRef.update("userScheduleList", userScheduleList)
//                                .addOnSuccessListener {
//                                    Log.d("Firestore", "✅ userScheduleList 업데이트 완료")
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.e("Firestore", "❌ userScheduleList 업데이트 실패", e)
//                                }
//                        }
//                    }.addOnFailureListener { e ->
//                        Log.e("Firestore", "❌ UserData 문서 조회 실패", e)
//                    }
//
//                    // application.navHostController.popBackStack()
////                    application.navHostController.navigate(
////                        "${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
////                                "tripScheduleDocId=${newScheduleDocId}&areaName=${scheduleCity}&areaCode=${areaCode}"
////                    )
//                    viewModelScope.launch {
//
//                        delay(300) // 0.2초 정도 딜레이 후 실행
//                        application.navHostController.popBackStack()
//                        application.navHostController.navigate(
//                            "${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
//                                    "tripScheduleDocId=${newScheduleDocId}&areaName=${scheduleCity}&areaCode=${areaCode}"
//                        )
//                    }
//
//                    Log.d("Schedule", "지역 이름: $scheduleCity, 지역 코드: $areaCode")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("Firestore", "❌ 새 일정 문서 생성 실패", e)
//                }
//        }
//    }

    fun goScheduleTitleButtonClick(tripNoteScheduleDocId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val tripNoteScheduleRef = firestore.collection("TripSchedule").document(tripNoteScheduleDocId)

        tripNoteScheduleRef.get().addOnSuccessListener { originalScheduleDoc ->
            if (!originalScheduleDoc.exists()) {
                Log.e("Firestore", "❌ 원본 일정 문서를 찾을 수 없음.")
                return@addOnSuccessListener
            }

            val scheduleCity = originalScheduleDoc.getString("scheduleCity") ?: return@addOnSuccessListener
            val originalStartDate = (originalScheduleDoc.get("scheduleStartDate") as? Timestamp)?.seconds ?: return@addOnSuccessListener
            val originalEndDate = (originalScheduleDoc.get("scheduleEndDate") as? Timestamp)?.seconds ?: return@addOnSuccessListener

            val scheduleDateList = generateDateRangeList(scheduleStartDate.value, scheduleEndDate.value)
            areaCode = AreaCode.values().find { it.areaName == scheduleCity }?.areaCode!!

            val newScheduleDocData = hashMapOf(
                "scheduleCity" to scheduleCity,
                "scheduleStartDate" to scheduleStartDate.value,
                "scheduleEndDate" to scheduleEndDate.value,
                "scheduleDateList" to scheduleDateList,
                "scheduleState" to 1,
                "scheduleTimeStamp" to Timestamp.now(),
                "scheduleTitle" to scheduleTitle.value,
                "userID" to application.loginUserModel.userId,
                "userNickName" to application.loginUserModel.userNickName,
                "scheduleInviteList" to listOf(application.loginUserModel.userDocId),
                "scheduleItems" to emptyList<Any>()
            )

            firestore.collection("TripSchedule").add(newScheduleDocData)
                .addOnSuccessListener { newScheduleDoc ->
                    val newScheduleRef = firestore.collection("TripSchedule").document(newScheduleDoc.id)
                    newScheduleRef.update("tripScheduleDocId", newScheduleDoc.id)
                        .addOnSuccessListener {
                            Log.d("Firestore", "✅ 새 일정 문서 생성 완료: ${newScheduleDoc.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "❌ tripScheduleDocId 업데이트 실패", e)
                        }

                    newScheduleDocId = newScheduleDoc.id
                    Log.d("Firestore", "✅ 새 일정 문서 ID: $newScheduleDocId")

                    tripNoteScheduleRef.collection("TripScheduleItem").get().addOnSuccessListener { snapshot ->
                        val adjustedItems = snapshot.documents
                            .mapNotNull { it.data?.toMutableMap() }
                            .map { item ->
                                val itemDate = (item["itemDate"] as? Timestamp)?.seconds ?: return@map null
                                val adjustedItemDate = getAdjustedItemDate(
                                    originalStartDate, originalEndDate, itemDate,
                                    scheduleStartDate.value.seconds, scheduleEndDate.value.seconds
                                )
                                item["itemDate"] = Timestamp(getDateOnly(adjustedItemDate), 0)
                                item
                            }
                            .filterNotNull()

                        val groupedItems = adjustedItems.groupBy { (it["itemDate"] as? Timestamp)?.seconds }
                        val itemTasks = mutableListOf<Task<Void>>()

                        groupedItems.forEach { (_, items) ->
                            items.forEachIndexed { index, item ->
                                item["itemIndex"] = index + 1
                                val task = newScheduleRef.collection("TripScheduleItem").add(item)
                                    .continueWithTask { newItemDoc ->
                                        newItemDoc.result?.update("itemDocId", newItemDoc.result?.id ?: "")
                                    }
                                itemTasks.add(task)
                            }
                        }

                        Tasks.whenAllSuccess<Void>(itemTasks).addOnSuccessListener {
                            Log.d("Firestore", "✅ 모든 일정 아이템 추가 완료")

                            val userDocRef = firestore.collection("UserData").document(application.loginUserModel.userDocId)
                            userDocRef.get().addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val userScheduleList = (userDoc.get("userScheduleList") as? MutableList<String>) ?: mutableListOf()
                                    userScheduleList.add(newScheduleDocId)

                                    userDocRef.update("userScheduleList", userScheduleList)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "✅ userScheduleList 업데이트 완료")

                                            viewModelScope.launch {
                                                delay(300)
                                                application.navHostController.popBackStack()
                                                application.navHostController.navigate(
                                                    "${ScheduleScreenName.SCHEDULE_DETAIL_SCREEN.name}?" +
                                                            "tripScheduleDocId=${newScheduleDocId}&areaName=${scheduleCity}&areaCode=${areaCode}"
                                                )
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore", "❌ userScheduleList 업데이트 실패", e)
                                        }
                                }
                            }.addOnFailureListener { e ->
                                Log.e("Firestore", "❌ UserData 문서 조회 실패", e)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "❌ 새 일정 문서 생성 실패", e)
                }
        }
    }


    // 날짜 변환 함수
    fun getAdjustedItemDate(
        originalStartDate: Long,
        originalEndDate: Long,
        itemDate: Long,
        newStartDate: Long,
        newEndDate: Long
    ): Long {
        // 원본 일정의 itemDate를 새 일정의 기간에 맞게 변환
        var adjustedItemDate = newStartDate + (itemDate - originalStartDate)

        // 새 일정의 마지막 날짜를 초과하는 경우, 새 일정의 마지막 날짜로 맞추기
        if (adjustedItemDate > newEndDate) {
            adjustedItemDate = newEndDate
        }

        return adjustedItemDate
    }


    // ✅ 날짜만 추출
    fun getDateOnly(timestamp: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+9"))
        calendar.timeInMillis = timestamp * 1000
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis / 1000
    }

}