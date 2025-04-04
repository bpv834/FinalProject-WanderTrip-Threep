package com.lion.wandertrip.presentation.schedule_detail_friends

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.lion.wandertrip.TripApplication
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.service.TripScheduleService
import com.lion.wandertrip.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailFriendsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val tripScheduleService: TripScheduleService,
    val userService: UserService,
) : ViewModel() {
    val application = context as TripApplication

    // 다이얼로그 표시 여부를 기억하는 상태
    val showAddFriendDialog = mutableStateOf(false)

    val showDeleteFriendDialog = mutableStateOf(false)

    // 일정 문서 ID
    val scheduleDocId = mutableStateOf("")

    // 친구 초대 에러 메시지 상태 (비어있으면 에러 없음)
    val friendAddError = mutableStateOf("")

    // 함께 하는 친구 목록
    val friendsDocIdList = mutableStateListOf<String>()

    // 함께 하는 친구 목록
    val friendsUserList = mutableStateListOf<UserModel>()

    // 일정 문서 ID 설정
    fun setScheduleDocId(id: String) {
        scheduleDocId.value = id
    }

    // 유저 일정 리스트들 옵저버
    fun observeScheduleDocIdList() {
        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("TripSchedule").document(scheduleDocId.value)

        // 문서 변경 감지 리스너 등록
        userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("observeUserData", "Error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                // userScheduleList 필드를 List<String> 형태로 가져오기 (없으면 빈 리스트)
                val schedule = snapshot.get("scheduleInviteList") as? List<String> ?: emptyList()

                // 기존 리스트 클리어 후 업데이트
                friendsDocIdList.clear()
                friendsDocIdList.addAll(schedule)
                Log.d("ScheduleDetailFriendsViewModel", "friendsList: $friendsDocIdList")

                formatInvitedUserDataToUserDocId()
            }
        }
    }

    // 이전 화면 으로 이동 (일정 상세 화면)
    fun backScreen() {
        application.navHostController.popBackStack()
    }

    // 결과에 따라 friendAddError와 showAddFriendDialog 상태를 업데이트함
    fun addFriendByNickName(inviteNickname: String) {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.addInviteUserByInviteNickname(
                    scheduleDocId.value,
                    inviteNickname
                )
            }.await()
            if (work1 == "") {
                Log.d("ScheduleDetailFriendsViewModel", "유저 없음 : $work1")
                friendAddError.value = "검색된 유저가 없습니다"
                // 다이얼로그는 계속 열어둠
            } else {
                Log.d("ScheduleDetailFriendsViewModel", "유저 있음 : $work1")
                val work2 = async(Dispatchers.IO) {
                    // 일정 항목의 초대 리스트에 유저 DocId 추가
                    tripScheduleService.addInviteUserDocIdToScheduleInviteList(
                        scheduleDocId.value,
                        work1
                    )
                }.await()
                val work3 = async(Dispatchers.IO) {
                    userService.addTripScheduleToUserSubCollection(work1, scheduleDocId.value)
                }
                work3.join()
                if (work2) {
                    friendAddError.value = ""
                    // 성공 시 다이얼로그 닫음
                    showAddFriendDialog.value = false
                } else {
                    friendAddError.value = "이미 추가된 유저입니다."
                }
            }
        }
    }

    // 초대된 유저 문서 Id로 유저 정보 가져오기
    fun formatInvitedUserDataToUserDocId() {
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.fetchUserScheduleList(friendsDocIdList)
            }.await()

            friendsUserList.clear()
            friendsUserList.addAll(work1)
        }
    }

    // 초대 받은 일정 삭제
    fun removeInvitedSchedule(invitedUser: String, tripScheduleDocId: String) {
        Log.d("ScheduleDetailFriendsViewModel", "invitedUser: $invitedUser")
        viewModelScope.launch {
            val work1 = async(Dispatchers.IO) {
                tripScheduleService.removeInvitedScheduleList(
                    invitedUser,
                    tripScheduleDocId
                )
            }.await()

            val work2 = async(Dispatchers.IO) {
                tripScheduleService.removeScheduleInviteList(
                    tripScheduleDocId,
                    invitedUser
                )
            }.await()

            val work3 = async(Dispatchers.IO) {
                userService.deleteTripScheduleItem(invitedUser, tripScheduleDocId)
            }
            work3.join()
        }
    }

}