package com.lion.wandertrip.service

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Timestamp
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.repository.TripScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripScheduleService(val tripScheduleRepository: TripScheduleRepository) {

    // 일정을 추가 하는 메서드
    suspend fun addTripSchedule(tripScheduleModel: TripScheduleModel) : String {
        // Model -> VO 변환
        val tripScheduleVO = tripScheduleModel.toTripScheduleVO()

        val docId = tripScheduleRepository.addTripSchedule(tripScheduleVO)
        Log.d("TripScheduleService", "docId: $docId")

        // Repository에 VO를 넘겨서 Firestore에 저장
        return docId
    }

    // 일정 문서 id를 유저 일정 리스트에 추가
    suspend fun addTripDocIdToUserScheduleList(userDocId: String, tripScheduleDocId: String) {
        tripScheduleRepository.addTripDocIdToUserScheduleList(userDocId, tripScheduleDocId)
    }

    // 일정 조회 (Firestore → VO 변환 → Model 변환)
    suspend fun getTripSchedule(docId: String): TripScheduleModel? {
        val tripScheduleVO = tripScheduleRepository.getTripSchedule(docId) ?: return null
        val tripScheduleModel = tripScheduleVO.toTripScheduleModel()
        Log.d("TripScheduleService", "getTripSchedule: 문서 $docId 조회 완료")
        return tripScheduleModel
    }

    // TripSchedule 서브 컬렉션의 모든 문서를 ScheduleItemVO 리스트로 조회
    suspend fun getTripScheduleItems(docId: String): List<ScheduleItem>? {
        val itemVOList = tripScheduleRepository.getTripScheduleItems(docId) ?: emptyList()
        return itemVOList.map { it.toScheduleItemModel() }
    }

    // flow 타입으로 리턴하는 메서드
    fun getTripScheduleItemModelsFlow(docId: String): Flow<List<ScheduleItem>> {
        return tripScheduleRepository.getTripScheduleItemsFlow(docId) // Flow<voList> -> flow<modelList> 작업
            .map { voList -> voList.map { it.toScheduleItemModel() } }
    }

    // 일정에 여행지 항목 추가
    suspend fun addTripItemToSchedule(docId: String, scheduleDate: Timestamp, scheduleItem: ScheduleItem) {
        val scheduleItemVO = scheduleItem.toScheduleItemVO()
        tripScheduleRepository.addTripItemToSchedule(docId, scheduleDate, scheduleItemVO)
    }

    // 일정 항목 삭제 후 itemIndex 재조정
    suspend fun removeTripScheduleItem(scheduleDocId: String, itemDocId: String, itemDate: Timestamp) {
        Log.d("removeTripScheduleItem", "scheduleDocId: $scheduleDocId, itemDocId: $itemDocId")
        tripScheduleRepository.removeTripScheduleItem(scheduleDocId, itemDocId)
    }

    // 일정 항목 문서 id로 일정 항목 가져 오기
    suspend fun getScheduleItemByDocId(tripScheduleDocId: String, scheduleItemDocId: String,): ScheduleItem? {
        val scheduleItemVO = tripScheduleRepository.getScheduleItemByDocId(tripScheduleDocId, scheduleItemDocId) ?: return null
        return scheduleItemVO.toScheduleItemModel()
    }

    // 일정 항목 업데이트
    suspend fun updateScheduleItem(
        tripScheduleDocId: String,
        scheduleItemDocId: String,
        updatedItem: ScheduleItem
    ) {
        val updatedItemVO = updatedItem.toScheduleItemVO()
        tripScheduleRepository.updateScheduleItem(tripScheduleDocId, scheduleItemDocId, updatedItemVO)
    }

    // 단일 Bitmap 업로드 -> 다운 로드 URL
    suspend fun uploadBitmapListToFirebase(bitmaps: List<Bitmap>): List<String> {
        return tripScheduleRepository.uploadBitmapListToFirebase(bitmaps)
    }

    // 위치 조정한 일정 항목 업데이트
    suspend fun updateItemsPosition(tripScheduleDocId: String, updatedItems: List<ScheduleItem>) {
        val updatedItemsVO = updatedItems.map { it.toScheduleItemVO() }
        tripScheduleRepository.updateItemsPosition(tripScheduleDocId, updatedItemsVO)
    }

    // 초대할 닉네임으로 유저 존재 여부 확인 후, 있으면 문서 ID 반환, 없으면 빈 문자열 반환
    suspend fun addInviteUserByInviteNickname(scheduleDocId: String, inviteNickname: String): String {
        val userDocId = tripScheduleRepository.addInviteUserByInviteNickname(scheduleDocId, inviteNickname)

        return userDocId
    }

    // 초대한 유저 문서 Id를 데이터에 추가
    suspend fun addInviteUserDocIdToScheduleInviteList(scheduleDocId: String, invitedUserDocId: String): Boolean {
        return tripScheduleRepository.addInviteUserDocIdToScheduleInviteList(scheduleDocId, invitedUserDocId)
    }

    // 유저 DocId 리스트로 유저 정보 가져오기
    suspend fun fetchUserScheduleList(userDocIdList: List<String>): List<UserModel> {
        return tripScheduleRepository.fetchUserScheduleList(userDocIdList).map { it.toUserModel() }
    }


    // 유저 일정 docId로 일정 항목 가져 오기
    suspend fun fetchScheduleList(scheduleDocIdList: List<String>): List<TripScheduleModel> {
        val scheduleItemList = tripScheduleRepository.fetchScheduleList(scheduleDocIdList)
        return scheduleItemList.map { it.toTripScheduleModel() }
    }

    // 유저 일정 리스트에서 일정 삭제
    suspend fun removeUserScheduleList(userDocId: String, userScheduleDocId: String) {
        tripScheduleRepository.removeUserScheduleList(userDocId, userScheduleDocId)
    }

    // 초대 받은 일정 리스트에서 일정 삭제
    suspend fun removeInvitedScheduleList(userDocId: String, invitedScheduleDocId: String) {
        tripScheduleRepository.removeInvitedScheduleList(userDocId, invitedScheduleDocId)
    }

    // 일정에서 초대된 유저 문서 Id 삭제
    suspend fun removeScheduleInviteList(tripScheduleDocId: String, userDocId: String) {
        tripScheduleRepository.removeScheduleInviteList(tripScheduleDocId, userDocId)
    }

    // 공공 데이터 관련 ///////////////////////////////////////////////////////////////////////////////

    // API 호출 및 데이터 로드
    suspend fun loadTripItems(serviceKey: String, areaCode: String, contentTypeId: String) : List<TripItemModel>? {
        val tripItemVOList = tripScheduleRepository.loadTripItems(serviceKey, areaCode, contentTypeId)
        val tripItemModelList = tripItemVOList?.map { it.toTripItemModel() }

        return tripItemModelList
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // hj
    // 내 스케줄 가져오기
    suspend fun gettingMyTripSchedules(userNickName:String) :MutableList<TripScheduleModel>{
     //   Log.d("test100","service -> gettingMyTripSchedules")

        val voList = tripScheduleRepository.gettingMyTripSchedules(userNickName)
        val result = mutableListOf<TripScheduleModel>()
        voList.forEach {
            result.add(it.toTripScheduleModel())
        }
        result.forEach {
         //   Log.d("test100","service -> result : ${it.scheduleTitle}")
        }


        return result
    }

    fun getMyTripSchedulesFlow(userNickName: String):Flow<List<TripScheduleModel>>{
        return tripScheduleRepository.getMyTripSchedulesFlow(userNickName).map { it.map { it.toTripScheduleModel() } }
    }


    //hj
    //닉네임 바꿀 때 사용하기
    // 닉변 전 게시물의 닉네임을 변경한 닉네임으로 update
    suspend fun changeTripScheduleNickName(oldNickName: String, newNickName: String) {
        tripScheduleRepository.changeTripScheduleNickName(oldNickName,newNickName)
    }

    // hj
    // 여행 삭제
    suspend fun deleteTripScheduleByDocId(docId : String) {

        tripScheduleRepository.deleteTripScheduleByDocId(docId)
    }
}