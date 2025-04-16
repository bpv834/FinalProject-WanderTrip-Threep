package com.lion.wandertrip.service

import android.net.Uri
import android.util.Log
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.model.TripNoteReplyModel
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.repository.TripNoteRepository
import com.lion.wandertrip.vo.TripNoteReplyVO
import com.lion.wandertrip.vo.TripNoteVO
import com.lion.wandertrip.vo.TripScheduleVO
import javax.inject.Inject

class TripNoteService @Inject constructor(val tripNoteRepository: TripNoteRepository) {

    // 이미지 데이터를 서버로 업로드 하는 메서드
    suspend fun uploadTripNoteImage(sourceFilePath:String, serverFilePath:String){
        tripNoteRepository.uploadTripNoteImage(sourceFilePath, serverFilePath)
    }

    // 여행기 데이터를 저장하는 메서드
    // 새롭게 추가된 문서의 id를 반환한다.
    suspend fun addTripNoteData(tripNoteModel: TripNoteModel) : String{
        // VO 객체를 생성한다.
        val tripNoteVO = tripNoteModel.toTripNoteVO()
        // 저장한다.
        val documentId = tripNoteRepository.addTripNoteData(tripNoteVO)
        return documentId
    }

    // 여행기 댓글을 저장하는 메서드
    // 새롭게 추가된 문서의 id를 반환한다.
    suspend fun addTripNoteReplyData(noteDocId: String,tripNoteReplyModel: TripNoteReplyModel) : String{
        // VO 객체를 생성한다.
        val tripNoteReplyVO = tripNoteReplyModel.toReplyItemVO()
        // 저장한다.
        val documentId = tripNoteRepository.addTripNoteReplyData(noteDocId,tripNoteReplyVO)
        return documentId?:""
    }

    // 여행기 댓글 리스트를 가져오는 메서드
    // 여행기 문서 id를 통해 데이터 가져오기
    suspend fun selectReplyDataOneById(documentId:String) : MutableList<TripNoteReplyModel>{
        // 데이터를 가져온다
        val tripNoteReplyList = mutableListOf<TripNoteReplyModel>()
        val resultList = tripNoteRepository.selectReplyDataOneById(documentId)

        resultList.forEach {
            val tripNoteReplyModel = it.toReplyItemModel()
            tripNoteReplyList.add(tripNoteReplyModel)
        }

        return tripNoteReplyList
    }

    // 여행기 리스트를 가져오는 메서드
    suspend fun gettingTripNoteList() : MutableList<TripNoteModel>{
        // 여행기 정보를 가져온다.
        val tripNoteList = mutableListOf<TripNoteModel>()
        val resultList = tripNoteRepository.gettingTripNoteList()

        resultList.forEach {
            val tripNoteVO = it["tripNoteVO"] as TripNoteVO
            val documentId = it["documentId"] as String
            val tripNoteImage = it["tripNoteImage"] as List<String>?
            val tripNoteModel = tripNoteVO.toTripNoteModel(documentId)
            tripNoteModel.tripNoteImage = tripNoteImage!!
            tripNoteList.add(tripNoteModel)
        }

        return tripNoteList
    }

    // 내 여행일정 가져오기
    suspend fun getTripSchedulesByUserDocId(userDocId : String) : MutableList<TripScheduleModel>{
        // 여행기 정보를 가져온다.
        val tripNoteList = mutableListOf<TripScheduleModel>()
        val resultList = tripNoteRepository.getTripSchedulesByUserDocId(userDocId)

        resultList.forEach {
            val tripNoteModel = it.toTripScheduleModel()
            tripNoteList.add(tripNoteModel)
        }

        return tripNoteList
    }

    // 내 다가오는 여행 일정 가져오기
    suspend fun gettingUpcomingScheduleList(userNickName : String) : MutableList<TripScheduleModel>{
        // 여행기 정보를 가져온다.
        val tripNoteList = mutableListOf<TripScheduleModel>()
        val resultList = tripNoteRepository.gettingUpcomingScheduleList(userNickName)

        resultList.forEach {
            val tripNoteVO = it["tripScheduleVO"] as TripScheduleVO
            // val documentId = it["documentId"] as String
            val tripNoteModel = tripNoteVO.toTripScheduleModel()
            tripNoteList.add(tripNoteModel)
        }

        return tripNoteList
    }

    suspend fun gettingTripNoteListWithScrapCount(): MutableList<TripNoteModel> {
        val tripNoteList = mutableListOf<TripNoteModel>()
        val resultList = tripNoteRepository.gettingTripNoteList()

        resultList.forEach {
            val tripNoteVO = it["tripNoteVO"] as TripNoteVO
            val documentId = it["documentId"] as String
            val tripNoteImage = it["tripNoteImage"] as List<String>?
            val tripNoteScrapCount = it["tripNoteScrapCount"] as? Int ?: 0 // ✅ 스크랩 수 가져오기

            val tripNoteModel = tripNoteVO.toTripNoteModel(documentId).apply {
                this.tripNoteImage = tripNoteImage ?: emptyList()
                this.tripNoteScrapCount = tripNoteScrapCount
            }

            tripNoteList.add(tripNoteModel)
        }

        return tripNoteList
    }


    // 일정 담아가면 담아가기 카운트 증가시키기
    suspend fun addTripNoteScrapCount(documentId: String){
        tripNoteRepository.addTripNoteScrapCount(documentId)
    }

    // 일정 조회 (Firestore → VO 변환 → Model 변환)
    suspend fun getTripSchedule(docId: String): TripScheduleModel? {
        val tripScheduleVO = tripNoteRepository.getTripSchedule(docId) ?: return null
        val tripScheduleModel = tripScheduleVO.toTripScheduleModel()
        Log.d("TripScheduleService", "getTripSchedule: 문서 $docId 조회 완료")
        return tripScheduleModel
    }

    // TripSchedule 서브 컬렉션의 모든 문서를 ScheduleItemVO 리스트로 조회
    suspend fun getTripScheduleItems(docId: String): List<ScheduleItem>? {
        val itemVOList = tripNoteRepository.getTripScheduleItems(docId) ?: emptyList()
        return itemVOList.map { it.toScheduleItemModel() }
    }



    // 해당 사람의 여행기 리스트 가져오기
    suspend fun gettingOtherTripNoteList(otherNickName : String) : MutableList<TripNoteModel>{
        // 여행기 정보를 가져온다.
        val tripNoteList = mutableListOf<TripNoteModel>()
        val resultList = tripNoteRepository.gettingOtherTripNoteList(otherNickName)

        resultList.forEach {
            val tripNoteVO = it["tripNoteVO"] as TripNoteVO
            val documentId = it["documentId"] as String
            val tripNoteModel = tripNoteVO.toTripNoteModel(documentId)
            tripNoteList.add(tripNoteModel)
        }

        return tripNoteList
    }

    // 여행기에 저장된 일정 id를 통해 일정 가져오기
    suspend fun gettingScheduleById(documentId:String) : TripScheduleModel{
        // 글 데이터를 가져온다.
        val tripScheduleVO = tripNoteRepository.gettingScheduleById(documentId)
        // BoardModel객체를 생성한다.
        val tripScheduleModel = tripScheduleVO.toTripScheduleModel()

        return tripScheduleModel
    }

    // 여행기 문서 id를 통해 데이터 가져오기
    suspend fun selectTripNoteDataOneById(documentId:String) : TripNoteModel?{
        // 글 데이터를 가져온다.
        val tripNoteVO = tripNoteRepository.selectTripNoteDataOneById(documentId)
        // BoardModel객체를 생성한다.
        val tripNoteModel = tripNoteVO?.toTripNoteModel(documentId)

        return tripNoteModel
    }

    // 이미지 데이터를 가져온다.
    suspend fun gettingImage(imageFileName:String) : Uri {
        val imageUri = tripNoteRepository.gettingImage(imageFileName)
        return imageUri
    }

    // 닉네임으로 프로필 사진 가져오기
    suspend fun gettingOtherProfileList(otherNickName:String) : Uri? {
        // 닉네임과 일치하는 사람 유저 문서에서 이미지 가져오기
        val imageUri = tripNoteRepository.gettingOtherProfileList(otherNickName)
        return imageUri
    }

    // 서버에서 댓글을 삭제한다.
    suspend fun deleteReplyData(documentId:String){
        tripNoteRepository.deleteReplyData(documentId)
    }

    // 서버에서 해당 여행기를 삭제한다
    suspend fun deleteTripNoteData(documentId:String){
        tripNoteRepository.deleteTripNoteData(documentId)
    }

    // 서버에서 이미지 파일을 삭제한다.
    suspend fun removeImageFile(imageFileName:String){
        tripNoteRepository.removeImageFile(imageFileName)
    }

    // 닉변시 사용할 메서드
    // 등록된 문서 닉네임 바꾸기
    suspend fun changeTripNoteNickname(oldNickName: String, newNickName: String) {
        tripNoteRepository.changeTripNoteNickname(oldNickName,newNickName)
    }

    // 이미지 데이터를 서버로 업로드 하는 메서드
    suspend fun uploadTripNoteImageList(sourceFilePath: List<String>, serverFilePath: MutableList<String>, noteTitle: String) :List<String>  {
        return tripNoteRepository.uploadTripNoteImageList(sourceFilePath,serverFilePath,noteTitle)
    }
}