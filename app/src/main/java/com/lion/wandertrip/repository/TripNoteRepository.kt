package com.lion.wandertrip.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.lion.wandertrip.vo.ScheduleItemVO
import com.lion.wandertrip.vo.TripNoteReplyVO
import com.lion.wandertrip.vo.TripNoteVO
import com.lion.wandertrip.vo.TripScheduleVO
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class TripNoteRepository@Inject constructor() {

    // 이미지 데이터를 서버로 업로드 하는 메서드
    suspend fun uploadTripNoteImage(sourceFilePath:String, serverFilePath:String){
        // 저장되어 있는 이미지의 경로
        val file = File(sourceFilePath)
        val fileUri = Uri.fromFile(file)
        // 업로드 한다.
        val firebaseStorage = FirebaseStorage.getInstance()
        val childReference = firebaseStorage.reference.child("image/$serverFilePath")
        childReference.putFile(fileUri).await()
    }

    // 여행기 데이터를 저장하는 메서드
    // 새롭게 추가된 문서의 id를 반환한다.
    suspend fun addTripNoteData(tripNoteVO: TripNoteVO) : String{
        val fireStore = FirebaseFirestore.getInstance()
        val collectionReference = fireStore.collection("TripNoteData")
        val documentReference = collectionReference.add(tripNoteVO).await()
        return documentReference.id
    }

    // 여행기 댓글을 저장하는 메서드
    // 새롭게 추가된 문서의 id를 반환한다.
    suspend fun addTripNoteReplyData(tripNoteReplyVO: TripNoteReplyVO) : String{
        val fireStore = FirebaseFirestore.getInstance()
        val collectionReference = fireStore.collection("TripNoteReplyData")
        val documentReference = collectionReference.add(tripNoteReplyVO).await()
        return documentReference.id
    }

    // 댓글 리스트를 가져오는 메서드
    suspend fun selectReplyDataOneById(documentId: String): MutableList<Map<String, *>> {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteReplyData")

        // 컬렉션에서 첫 번째 문서를 제한하여 가져온다 (최대 1개 문서)
        val data = collectionReference.limit(1).get().await()

        // 컬렉션이 비어있다면 빈 리스트를 반환
        if (data.isEmpty) {
            val resultList = mutableListOf<Map<String, *>>()
            return  resultList
        }

        // 데이터를 가져온다.
        val result =
            collectionReference
                .whereEqualTo("tripNoteDocumentId", documentId)
                .get()
                .await()

        // 반환할 리스트
        val resultList = mutableListOf<Map<String, *>>()


        // 데이터의 수 만큼 반환한다
        val sortedResult = result.documents.sortedByDescending { it.getTimestamp("replyTimeStamp")?.toDate() }

            // 데이터의 수 만큼 반환한다.
            sortedResult.forEach {
            val tripNoteReplyVO = it.toObject(TripNoteReplyVO::class.java)
            val map = mapOf(
                // 문서의 id
                "tripNoteDocumentId" to it.id,
                // 데이터를 가지고 있는 객체
                "tripNoteReplyVO" to tripNoteReplyVO,
            )
            resultList.add(map)
        }
        return resultList
    }

    // 여행기 리스트 가져오는 메서드
    suspend fun gettingTripNoteList(): MutableList<Map<String, *>> {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteData")
        // 데이터를 가져온다.
        val result =
            collectionReference
                .orderBy("tripNoteTimeStamp", Query.Direction.DESCENDING).get()
                .await()
        // 반환할 리스트
        val resultList = mutableListOf<Map<String, *>>()
        // 데이터의 수 만큼 반환한다.
        result.forEach {
            val tripNoteVO = it.toObject(TripNoteVO::class.java) // TripNoteVO 객체 가져오기
            val tripNoteImage = tripNoteVO.tripNoteImage
            val map = mapOf(
                // 문서의 id
                "documentId" to it.id,
                // 데이터를 가지고 있는 객체
                "tripNoteVO" to tripNoteVO,
                // 이미지 리스트
                "tripNoteImage" to tripNoteImage
            )
            resultList.add(map)
        }
        return resultList
    }

//    // 닉네임을 통해 유저의 일정 리스트를 가져오는 메서드
//    suspend fun gettingUserScheduleList(userNickName : String): MutableList<Map<String, *>> {
//        val firestore = FirebaseFirestore.getInstance()
//        val collectionReference = firestore.collection("TripSchedule")
//        // 데이터를 가져온다.
//        val result =
//            collectionReference
//                .whereEqualTo("userNickName", userNickName)
//                //.orderBy("scheduleTimeStamp", Query.Direction.DESCENDING)
//                .get()
//                .await()
//        // 반환할 리스트
//        val resultList = mutableListOf<Map<String, *>>()
//        // 데이터의 수 만큼 반환한다.
//        result.forEach {
//            val tripScheduleVO = it.toObject(TripScheduleVO::class.java) // TripNoteVO 객체 가져오기
//            val map = mapOf(
//                // 문서의 id
//                "documentId" to it.id,
//                // 데이터를 가지고 있는 객체
//                "tripScheduleVO" to tripScheduleVO,
//            )
//            resultList.add(map)
//        }
//        return resultList
//    }

    // 유저 문서 아이디로 일정 가져오기
    suspend fun getTripSchedulesByUserDocId(userDocId: String): MutableList<TripScheduleVO> {
        val firestore = FirebaseFirestore.getInstance()

        // 1️⃣ UserScheduleData 서브컬렉션에서 tripScheduleDocId 리스트 추출
        val userSchedules = firestore.collection("UserData")
            .document(userDocId)
            .collection("UserScheduleData")
            .get()
            .await()

        // tripScheduleDocId 필드만 모아 리스트 생성
        val tripScheduleIdList = userSchedules.mapNotNull { it.getString("tripScheduleDocId") }

        if (tripScheduleIdList.isEmpty()) return mutableListOf()

        val resultList = mutableListOf<TripScheduleVO>()

        // 2️⃣ 10개씩 나눠서 whereIn으로 TripSchedule 조회
        tripScheduleIdList.chunked(10).forEach { idChunk ->
            val snapshot = firestore.collection("TripSchedule")
                .whereIn(FieldPath.documentId(), idChunk)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val tripScheduleVO = doc.toObject(TripScheduleVO::class.java)
                resultList.add(tripScheduleVO?:TripScheduleVO())
            }

            // 3️⃣ 리스트 개수와 일치하면 바로 반환
            if (resultList.size >= tripScheduleIdList.size) return@forEach
        }

        return resultList
    }



    // 닉네임을 통해 유저의 다가오는 일정 리스트를 가져오는 메서드
//    suspend fun gettingUpcomingScheduleList(userNickName: String): MutableList<Map<String, *>> {
//        val firestore = FirebaseFirestore.getInstance()
//        val collectionReference = firestore.collection("TripSchedule")
//
//        // 현재 시간 가져오기
//        val currentTime = Timestamp.now()
//
//        // 닉네임 필터링으로 데이터 가져오기
//        val nicknameFilteredResult =
//            collectionReference
//                .whereEqualTo("userNickName", userNickName)
//                .get()
//                .await()
//
//        // 반환할 리스트
//        val resultList = mutableListOf<Map<String, *>>()
//
//        // 닉네임 필터링된 데이터 중에서 날짜 조건을 만족하는 데이터만 추가
//        nicknameFilteredResult.forEach {
//            val tripScheduleVO = it.toObject(TripScheduleVO::class.java)
//
//            // scheduleStartDate가 현재 시간보다 큰 경우만 추가
//            if (tripScheduleVO.scheduleStartDate > currentTime) {
//                val map = mapOf(
//                    // 문서의 id
//                    "documentId" to it.id,
//                    // 데이터를 가지고 있는 객체
//                    "tripScheduleVO" to tripScheduleVO,
//                )
//                resultList.add(map)
//            }
//        }
//
//        return resultList
//    }

    // 닉네임을 통해 유저의 다가오는 일정 리스트를 가져오는 메서드
    suspend fun gettingUpcomingScheduleList(userNickName: String): MutableList<Map<String, *>> {
        val firestore = FirebaseFirestore.getInstance()

        // 현재 시간 가져오기
        val currentTime = Timestamp.now()

        // 유저 데이터 가져오기
        val userDataSnapshot = firestore.collection("UserData")
            .whereEqualTo("userNickName", userNickName)
            .get()
            .await()

        if (userDataSnapshot.isEmpty) return mutableListOf() // 유저 데이터가 없으면 빈 리스트 반환

        // userScheduleList 필드 가져오기
        val userScheduleList = userDataSnapshot.documents.first()
            .get("userScheduleList") as? List<String> ?: return mutableListOf()

        // 반환할 리스트
        val resultList = mutableListOf<Map<String, *>>()

        // TripSchedule 컬렉션에서 일정 문서 가져오기
        val tripScheduleSnapshot = firestore.collection("TripSchedule")
            .whereIn(FieldPath.documentId(), userScheduleList)
            .get()
            .await()

        tripScheduleSnapshot.forEach { document ->
            val tripScheduleVO = document.toObject(TripScheduleVO::class.java)

            // scheduleStartDate가 현재 시간보다 큰 경우만 추가
            if (tripScheduleVO.scheduleStartDate > currentTime) {
                val map = mapOf(
                    "documentId" to document.id,
                    "tripScheduleVO" to tripScheduleVO
                )
                resultList.add(map)
            }
        }

        return resultList
    }




    // 일정 담아가면 그 카운트 증가시키기
    suspend fun addTripNoteScrapCount(documentId: String){
        // documentId에 해당하는 여행기 문서 찾기
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteData")
        val documentReference = collectionReference.document(documentId)

        // 카운트 1씩 증가시키기
        documentReference.update("tripNoteScrapCount", FieldValue.increment(1))
            .await()
    }

    suspend fun gettingTripScrapeCount(): MutableList<Map<String, *>> {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteData")

        // 데이터를 가져온다.
        val result = collectionReference
            .orderBy("tripNoteTimeStamp", Query.Direction.DESCENDING).get()
            .await()

        // 반환할 리스트
        val resultList = mutableListOf<Map<String, *>>()

        // 데이터의 수 만큼 반환한다.
        result.forEach {
            val tripNoteVO = it.toObject(TripNoteVO::class.java) // TripNoteVO 객체 가져오기
            val tripNoteImage = tripNoteVO.tripNoteImage
            val tripNoteScrapCount = it.getLong("tripNoteScrapCount")?.toInt() ?: 0 // ✅ 스크랩 수 추가

            val map = mapOf(
                "documentId" to it.id,        // 문서 ID
                "tripNoteVO" to tripNoteVO,  // 여행기 데이터 객체
                "tripNoteImage" to tripNoteImage, // 이미지 리스트
                "tripNoteScrapCount" to tripNoteScrapCount // ✅ 스크랩 수 추가
            )

            resultList.add(map)
        }
        return resultList
    }


    // 일정 조회 (VO 리턴)
    suspend fun getTripSchedule(docId: String): TripScheduleVO? {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("TripSchedule").document(docId)

        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            // 스냅샷을 VO로 변환
            return snapshot.toObject(TripScheduleVO::class.java)
        }
        return null
    }

    // TripSchedule 서브 컬렉션의 모든 문서를 ScheduleItemVO 리스트로 조회
    suspend fun getTripScheduleItems(docId: String): List<ScheduleItemVO>? {
        val firestore = FirebaseFirestore.getInstance()
        val subCollectionRef = firestore.collection("TripSchedule")
            .document(docId)
            .collection("TripScheduleItem")

        val snapshot = subCollectionRef.get().await()
        if (!snapshot.isEmpty) {
            return snapshot.toObjects(ScheduleItemVO::class.java)
        }
        return emptyList()
    }


    // 닉네임을 통해 다른 사람 여행기 리스트를 가져온다
    suspend fun gettingOtherTripNoteList(otherNickName : String): MutableList<Map<String, *>> {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteData")
        // 데이터를 가져온다.
        val result =
            collectionReference
                .whereEqualTo("userNickname", otherNickName)
                //.orderBy("scheduleTimeStamp", Query.Direction.DESCENDING)
                .get()
                .await()
        // 반환할 리스트
        val resultList = mutableListOf<Map<String, *>>()
        // 데이터의 수 만큼 반환한다.
        result.forEach {
            val tripNoteVO = it.toObject(TripNoteVO::class.java) // TripNoteVO 객체 가져오기
            val map = mapOf(
                // 문서의 id
                "documentId" to it.id,
                // 데이터를 가지고 있는 객체
                "tripNoteVO" to tripNoteVO,
            )
            resultList.add(map)
        }
        return resultList
    }



    suspend fun selectTripNoteDataOneById(documentId: String): TripNoteVO? {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteData")
        val documentReference = collectionReference.document(documentId)
        val documentSnapShot = documentReference.get().await()

        // 문서가 존재하는지 확인
        if (documentSnapShot.exists()) {
            val tripNoteVO = documentSnapShot.toObject(TripNoteVO::class.java)
            return tripNoteVO // null이 아닐 경우 반환
        } else {
            Log.e("TripNote", "Document with ID $documentId does not exist.")
            return null // 문서가 없으면 null 반환
        }
    }



    suspend fun gettingScheduleById(documentId:String) : TripScheduleVO{
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripSchedule")
        val documentReference = collectionReference.document(documentId)
        val documentSnapShot = documentReference.get().await()
        val tripScheduleVO = documentSnapShot.toObject(TripScheduleVO::class.java)!!
        return tripScheduleVO
    }

    // 이미지 데이터를 가져온다
    suspend fun gettingImage(imageFileName:String) : Uri{
        val storageReference = FirebaseStorage.getInstance().reference
        // 파일명을 지정하여 이미지 데이터를 가져온다.
        val childStorageReference = storageReference.child("image/$imageFileName")
        val imageUri = childStorageReference.downloadUrl.await()
        return imageUri
    }

    // 닉네임과 일치하는 유저의 이미지 데이터 가져오기
    suspend fun gettingOtherProfileList(otherNickName:String) : Uri? {
        val firestore = FirebaseFirestore.getInstance()

        // / 닉네임이 일치하는 문서 찾기
        val collectionReference = firestore.collection("UserData")
            .whereEqualTo("userNickName", otherNickName)
            .get()
            .await()

        // 일치하는 문서가 있는지 확인
        val documentSnapshot = collectionReference.documents.firstOrNull()
            ?: return null // 문서가 없으면 null 반환

        // 해당 문서에서 프로필 이미지 경로 가져오기
        val userProfileImageURL = documentSnapshot.getString("userProfileImageURL")

        // userProfileImageURL이 null 또는 비어 있다면 null 반환
        if (userProfileImageURL.isNullOrEmpty()) return null


        val storageReference = FirebaseStorage.getInstance().reference
        // 파일명을 지정하여 이미지 데이터를 가져온다.
        val childStorageReference = storageReference.child("userProfileImage/$userProfileImageURL")
        val imageUri = childStorageReference.downloadUrl.await()

        return imageUri
    }

    // 서버에서 댓글을 삭제한다.
    suspend fun deleteReplyData(documentId:String){
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteReplyData")
        val documentReference = collectionReference.document(documentId)
        documentReference.delete().await()
    }

    // 서버에서 여행기를 삭제한다.
    suspend fun deleteTripNoteData(documentId:String){
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteData")
        val documentReference = collectionReference.document(documentId)
        documentReference.delete().await()
    }

    // 서버에서 이미지 파일을 삭제한다.
    suspend fun removeImageFile(imageFileName:String){
        val imageReference = FirebaseStorage.getInstance().reference.child("image/$imageFileName")
        imageReference.delete().await()
    }


    // 닉변 시 사용하는 메서드,
    // TripNoteData 컬렉션에서 기존 닉네임을 새로운 닉네임으로 변경
    suspend fun changeTripNoteNickname(oldNickName: String, newNickName: String) {
        val firestore = FirebaseFirestore.getInstance()
        val collRef = firestore.collection("TripNoteData")

        try {
            val querySnapshot = collRef.whereEqualTo("userNickname", oldNickName).get().await()

            if (querySnapshot.isEmpty) {
                Log.d("test100", "변경할 닉네임($oldNickName)이 존재하지 않습니다.")
                return
            }

            for (document in querySnapshot.documents) {
                val docRef = collRef.document(document.id)
                docRef.update("userNickname", newNickName).await()
            }
        } catch (e: Exception) {
            Log.e("test100", "닉네임 변경 중 오류 발생: $e", e)
        }
    }

    // 이미지 데이터를 서버로 업로드 하는 메서드
    suspend fun uploadTripNoteImageList(
        sourceFilePath: List<String>, // 업로드할 이미지 파일 경로 목록
        serverFilePath: List<String>, // 서버에 저장될 파일 이름 목록
        noteTitle: String // 해당 콘텐츠의 ID
    ): List<String> { // 반환 타입을 List<String>으로 변경하여 이미지 다운로드 URL을 반환
        // 업로드된 이미지의 URL들을 저장할 리스트
        val downloadUrls = mutableListOf<String>()

        // 리스트의 각 파일에 대해 업로드 작업을 순차적으로 수행
        for (i in sourceFilePath.indices) {
            val sourceFile = File(sourceFilePath[i])  // 소스 파일 경로
            val fileUri = Uri.fromFile(sourceFile)

            Log.d("FirebaseStorage", "업로드 중: ${sourceFile.path}, 존재 여부: ${sourceFile.exists()}")

            // Firebase Storage의 경로 설정
            val firebaseStorage = FirebaseStorage.getInstance()
            val childReference = firebaseStorage.reference.child("tripNoteImage/$noteTitle/${serverFilePath[i]}")

            try {
                Log.d("FirebaseStorage", "업로드 시작: ${fileUri.path} -> ${childReference.path}")

                // 파일 업로드
                val uploadTask = childReference.putFile(fileUri).await()

                Log.d("FirebaseStorage", "업로드 성공: ${fileUri.path}")

                // 업로드 완료 후 다운로드 URL 가져오기
                val downloadUrl = childReference.downloadUrl.await().toString()

                Log.d("FirebaseStorage", "다운로드 URL: $downloadUrl")

                // 다운로드 URL을 리스트에 추가
                downloadUrls.add(downloadUrl)
            } catch (e: Exception) {
                // 업로드 실패 시 로그 출력
                Log.e("FirebaseStorage", "파일 업로드 실패: ${sourceFile.path}", e)
            }
        }

        // 최종적으로 다운로드 URL 리스트를 반환
        return downloadUrls
    }

}