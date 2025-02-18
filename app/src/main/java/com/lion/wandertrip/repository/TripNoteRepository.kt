package com.lion.wandertrip.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.lion.wandertrip.model.TripNoteModel
import com.lion.wandertrip.vo.TripNoteReplyVO
import com.lion.wandertrip.vo.TripNoteVO
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

        // 데이터를 가져온다.
        val result =
            collectionReference
                .whereEqualTo("tripNoteDocumentId", documentId)
//                .orderBy("tripNoteTimeStamp", Query.Direction.DESCENDING)
                .get()
                .await()

        // 반환할 리스트
        val resultList = mutableListOf<Map<String, *>>()

        // 데이터의 수 만큼 반환한다
        val sortedResult = result.documents.sortedByDescending { it.getTimestamp("tripNoteTimeStamp")}

            // 데이터의 수 만큼 반환한다.
            sortedResult.forEach {
            val tripNoteReplyVO = it.toObject(TripNoteReplyVO::class.java)
            val map = mapOf(
                // 문서의 id
                "documentId" to it.id,
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
            collectionReference.orderBy("tripNoteTimeStamp", Query.Direction.DESCENDING).get()
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

    // 여행기 문서 id를 통해 데이터 가져오기
    suspend fun selectTripNoteDataOneById(documentId:String) : TripNoteVO{
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripNoteData")
        val documentReference = collectionReference.document(documentId)
        val documentSnapShot = documentReference.get().await()
        val tripNoteVO = documentSnapShot.toObject(TripNoteVO::class.java)!!
        return tripNoteVO
    }

    // 이미지 데이터를 가져온다
    suspend fun gettingImage(imageFileName:String) : Uri{
        val storageReference = FirebaseStorage.getInstance().reference
        // 파일명을 지정하여 이미지 데이터를 가져온다.
        val childStorageReference = storageReference.child("image/$imageFileName")
        val imageUri = childStorageReference.downloadUrl.await()
        return imageUri
    }
}