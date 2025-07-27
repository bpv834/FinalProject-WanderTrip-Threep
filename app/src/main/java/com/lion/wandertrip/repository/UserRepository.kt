package com.lion.wandertrip.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.lion.wandertrip.util.UserState
import com.lion.wandertrip.vo.UserVO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File

class UserRepository {

    // userID로 userVO찾기
    suspend fun selectUserDataByUserId(userId: String): UserVO? {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")

        // 쿼리 결과 가져오기
        val result = collectionReference.whereEqualTo("userId", userId).get().await()

        // 데이터가 없다면 null 반환
        if (result.isEmpty) {
            Log.d("UserData", "사용자 아이디 '$userId'에 해당하는 사용자가 없습니다.")
            return null
        }

        // 데이터가 있을 경우 첫 번째 사용자 정보 반환
        return result.toObjects(UserVO::class.java).firstOrNull()
    }

    // 사용자 닉네임을 통해 사용자 데이터를 가져오는 메서드
    suspend fun selectUserDataByUserNickName(userNickName: String): MutableList<UserVO> {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")
        val result = collectionReference.whereEqualTo("userNickName", userNickName).get().await()
        val userVoList = result.toObjects(UserVO::class.java)
        return userVoList
    }

    fun addUserData(userVO: UserVO): String {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")
        val documentReference = collectionReference.document()
        userVO.userDocId = documentReference.id

        Log.d("Firestore", "생성된 문서 ID: ${documentReference.id}")
        Log.d("Firestore", "추가할 유저 데이터: $userVO")

        documentReference.set(userVO)
            .addOnSuccessListener {
                Log.d("Firestore", "유저 데이터 추가 성공! 문서 ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "유저 데이터 추가 실패: ${e.message}", e)
            }

        return documentReference.id
    }
    // 사용자 아이디와 동일한 사용자의 정보 하나를 반환하는 메서드
    suspend fun selectUserDataByUserIdOne(userId: String): UserVO? {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")

        val result = collectionReference.whereEqualTo("userId", userId).get().await()
        val userVoList = result.toObjects(UserVO::class.java)

        Log.d("UserRepository", "Requested userId: $userId")
        Log.d("UserRepository", "Fetched document count: ${result.documents.size}")
        Log.d("UserRepository", "Parsed UserVO list size: ${userVoList.size}")

        if (userVoList.size != 0) {
            val user = userVoList[0]
            Log.d("UserRepository", "Fetched user: $user")
            return user
        } else {
            Log.e("UserRepository", "No user found with userId: $userId")
            Log.d("UserRepository", "no user")
            return null
        }
    }


    // 자동로그인 토큰값을 갱신하는 메서드
    suspend fun updateUserAutoLoginToken(userDocumentId: String, newToken: String) {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")
        val documentReference = collectionReference.document(userDocumentId)
        val tokenMap = mapOf(
            "userAutoLoginToken" to newToken
        )
        documentReference.update(tokenMap).await()
    }

    //Todo 에러 예외 처리 필요함
    // 자동 로그인 토큰 값으로 사용자 정보를 가져오는 메서드
    suspend fun selectUserDataByLoginToken(loginToken: String): Map<String, *>? {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")
        val resultList =
            collectionReference.whereEqualTo("userAutoLoginToken", loginToken).get().await()
        val userVOList = resultList.toObjects(UserVO::class.java)
        if (userVOList.isEmpty()) {
            return null
        } else {
            val userDocumentId = resultList.documents[0].id
            val returnMap = mapOf(
                "userDocumentId" to userDocumentId,
                "userVO" to userVOList[0]
            )
            return returnMap
        }
    }

    // 카카오 로그인 토큰 값으로 사용자 정보를 가져오는 메서드
    suspend fun selectUserDataByKakaoLoginToken(kToken: Long): UserVO? {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")

        return try {
            // Firestore에서 데이터를 조회
            val result = collectionReference.whereEqualTo("kakaoId", kToken).get().await()

            // 데이터를 객체로 변환하여 반환
            val userVo = result.toObjects(UserVO::class.java).firstOrNull()

            // 결과가 없으면 null 반환
            if (userVo == null) {
                Log.d("test100", "No user found for the given Kakao token")
            }

            userVo
        } catch (e: Exception) {
            // 예외 발생 시 로그 출력
            Log.e("test100", "Failed to fetch user data by Kakao token", e)
            null  // 예외 발생 시 null 반환
        }
    }

    // 사용자 정보 전체를 가져오는 메서드
    suspend fun selectUserDataAll(): MutableList<MutableMap<String, *>> {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")
        val result = collectionReference.get().await()
        val userList = mutableListOf<MutableMap<String, *>>()
        result.forEach {
            val userMap = mutableMapOf(
                "user_document_id" to it.id,
                "user_vo" to it.toObject(UserVO::class.java)
            )
            userList.add(userMap)
        }
        return userList
    }

    // 사용자 문서 아이디를 통해 사용자 정보를 가져온다.
    suspend fun selectUserDataByUserDocumentIdOne(userDocumentId: String): UserVO {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")
        val result = collectionReference.document(userDocumentId).get().await()
        val userVO = result.toObject(UserVO::class.java)!!
        return userVO
    }


    // 사용자 데이터를 수정한다.
    suspend fun updateUserData(userVO: UserVO) {
        // Firestore 인스턴스 가져오기
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")

        // 수정할 문서에 접근
        val documentReference = collectionReference.document(userVO.userDocId)

        try {
            // UserVO 객체 그대로 업데이트 (set 사용)
            documentReference.set(userVO).await()  // set() 메서드로 데이터 저장
            Log.d("Firestore", "사용자 데이터 업데이트 성공")
        } catch (e: Exception) {
            Log.e("Firestore", "사용자 데이터 업데이트 실패", e)
        }
    }

    suspend fun updateUserLikeList(userDocumentId: String, userLikeList: List<String>) {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")
        val documentReference = collectionReference.document(userDocumentId)

        try {
            // ✅ Firestore의 특정 필드(userLikeList)만 업데이트
            documentReference.update("userLikeList", userLikeList).await()
            Log.d("Firestore", "사용자 관심 목록 업데이트 성공")
        } catch (e: Exception) {
            Log.e("Firestore", "사용자 관심 목록 업데이트 실패", e)
        }
    }

    // 사용자의 상태를 변경하는 메서드
    suspend fun updateUserState(userDocumentId: String, newState: UserState) {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")
        val documentReference = collectionReference.document(userDocumentId)

        val updateMap = mapOf(
            "userState" to newState.number
        )

        documentReference.update(updateMap).await()
    }

    // userDocID 로 user 정보 가져오기
    suspend fun getUserByUserDocId(userDocId: String): UserVO? {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("UserData")

        Log.d("test200", "getUserByUserDocId() 호출됨 - userDocId: $userDocId")

        return try {
            val documentSnapshot = collectionReference.document(userDocId).get().await()

            if (documentSnapshot.exists()) {
                Log.d("test200", "문서 존재함 - userDocId: $userDocId")
                val user = documentSnapshot.toObject(UserVO::class.java)
                Log.d("test200", "변환된 UserVO: $user")
                user // Firestore 데이터를 UserVO 객체로 변환
            } else {
                Log.d("test200", "문서 없음 - userDocId: $userDocId")
                null
            }
        } catch (e: Exception) {
            Log.e("test200", "오류 발생 - userDocId: $userDocId", e)
            e.printStackTrace()
            null
        }
    }

    // userDocID로 Firestore에서 userLikeList 필드만 가져오는 함수
    suspend fun getUserLikeList(userDocId: String): List<String> {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore
            .collection("UserData")
            .document(userDocId)
            .collection("UserLikeList")

        return try {
            val querySnapshot = collectionReference.get().await()

            // 🔥 각 문서의 "contentId" 필드만 꺼내기
            val likeList = querySnapshot.documents.mapNotNull { it.getString("contentId") }

            Log.d("getUserLikeList", "서브컬렉션에서 가져온 contentId 리스트: $likeList")
            likeList
        } catch (e: Exception) {
            Log.e("getUserLikeList", "오류 발생 - userDocId: $userDocId", e)
            e.printStackTrace()
            emptyList()
        }
    }


    // 이미지 데이터를 서버로 업로드 하는 메서드
    suspend fun uploadImage(sourceFilePath: String, serverFilePath: String) {
        // 저장되어 있는 이미지의 경로
        val file = File(sourceFilePath)
        val fileUri = Uri.fromFile(file)
        // 업로드 한다.
        val firebaseStorage = FirebaseStorage.getInstance()
        val childReference = firebaseStorage.reference.child("userProfileImage/$serverFilePath")
        childReference.putFile(fileUri).await()
    }

    // 이미지 Uri 가져온다.
    // 이미지 데이터를 가져온다.
    suspend fun gettingImage(imageFileName: String): Uri? {
        Log.d("gettingImage", "이미지 파일명을 받음: $imageFileName")

        val storageReference = FirebaseStorage.getInstance().reference
        Log.d("gettingImage", "Firebase Storage 레퍼런스 초기화됨")

        val childStorageReference = storageReference.child("userProfileImage/$imageFileName")
        Log.d("gettingImage", "이미지 파일 경로: userProfileImage/$imageFileName")

        return try {
            val imageUri = childStorageReference.downloadUrl.await()
            Log.d("gettingImage", "이미지 URI 가져옴: $imageUri")
            imageUri
        } catch (e: Exception) {
            if (e is StorageException && e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                Log.w("gettingImage", "이미지가 존재하지 않습니다: $imageFileName")
                null // 이미지가 없을 경우 null 반환
            } else {
                Log.e("gettingImage", "이미지 URI 가져오기 실패: ${e.message}")
                throw e // 다른 예외는 그대로 던짐
            }
        }
    }

    // 사용자의 관심 콘텐츠 ID 리스트를 가져오는 함수
    suspend fun gettingUserInterestingContentIdList(userDocId: String): List<String> {
        Log.d("UserRepo", "gettingUserInterestingContentIdList")
        return try {
            val firebase = FirebaseFirestore.getInstance()
            val contentIdList = mutableListOf<String>()

            // 1. UserData 컬렉션에서 해당 userDocId 문서의 UserLikeList 서브컬렉션 접근
            val documents = firebase.collection("UserData")
                .document(userDocId)
                .collection("UserLikeList")
                .get()
                .await() // suspend 사용 (비동기)

            // 2. 모든 문서의 contentId 필드 값을 가져와 리스트에 추가
            for (document in documents) {
                val contentId = document.getString("contentId")
                if (contentId != null) {
                    contentIdList.add(contentId)
                }
            }

            Log.d("test100", " 사용자 관심 콘텐츠 ID 리스트: $contentIdList")
            contentIdList // 최종 리스트 반환

        } catch (e: Exception) {
            Log.e("test100", " 관심 콘텐츠 ID 가져오기 실패: $userDocId", e)
            emptyList() // 예외 발생 시 빈 리스트 반환
        }
    }

    // 관심 지역(또는 콘텐츠) 추가
    suspend fun addLikeItem(userDocId: String, likeItemContentId: String) {

        Log.d("addLikeItem", "userDocId: $userDocId, likeItemContentId: $likeItemContentId")
        val firestore = FirebaseFirestore.getInstance()
        // 루트 컬렉션은 "UserData"이어야 함
        val subCollectionRef = firestore.collection("UserData")
            .document(userDocId)
            .collection("UserLikeList")

        // 먼저, 같은 콘텐츠 ID가 이미 있는지 확인
        val querySnapshot = subCollectionRef
            .whereEqualTo("contentId", likeItemContentId)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            // 이미 존재하면 추가하지 않음
        } else {
            // 존재하지 않으면 새 문서를 추가
            subCollectionRef.add(mapOf("contentId" to likeItemContentId)).await()
        }
    }

    // 관심 지역(또는 콘텐츠) 카운트 증가
    suspend fun addLikeCnt(likeItemContentId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("ContentsData")

        // contentId 필드가 likeItemContentId와 일치하는 문서를 쿼리
        val querySnapshot = collectionReference
            .whereEqualTo("contentId", likeItemContentId)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            // 이미 존재하는 문서들의 interestingCount를 1 증가
            for (document in querySnapshot.documents) {
                document.reference.update("interestingCount", FieldValue.increment(1))
                    .await()
            }
            Log.d(
                "addLikeCnt",
                "interestingCount incremented for contentId: $likeItemContentId"
            )
        } else {
            // 문서가 없으면 새 문서를 생성 (초기값: interestingCount = 1, 나머지는 기본값)
            val newDoc = hashMapOf(
                "contentId" to likeItemContentId,
                "ratingScore" to 0.0f,
                "reviewList" to emptyList<Any>(),
                "getRatingCount" to 0,
                "interestingCount" to 1
            )
            collectionReference.add(newDoc).await()
            Log.d(
                "addLikeCnt",
                "New document created for contentId: $likeItemContentId with interestingCount = 1"
            )
        }
    }


    // 관심 지역(또는 콘텐츠) 삭제
    suspend fun removeLikeItem(userDocId: String, likeItemContentId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val subCollectionRef = firestore.collection("UserData")
            .document(userDocId)
            .collection("UserLikeList")

        // 같은 콘텐츠 ID가 있는 문서 쿼리
        val querySnapshot = subCollectionRef
            .whereEqualTo("contentId", likeItemContentId)
            .get()
            .await()

        // 쿼리 결과가 비어있지 않으면 해당 문서 삭제
        if (!querySnapshot.isEmpty) {
            for (doc in querySnapshot.documents) {
                doc.reference.delete().await()
            }
        }
    }

    // 관심 지역(또는 콘텐츠) 카운트 감소
    suspend fun removeLikeCnt(likeItemContentId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("ContentsData")

        // contentId 필드가 likeItemContentId와 일치하는 문서를 쿼리
        val querySnapshot = collectionReference
            .whereEqualTo("contentId", likeItemContentId)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            for (document in querySnapshot.documents) {
                document.reference.update("interestingCount", FieldValue.increment(-1))
                    .await()
            }
            Log.d(
                "removeLikeCnt",
                "Decremented interestingCount for contentId: $likeItemContentId"
            )
        } else {
            Log.d("removeLikeCnt", "No document found with contentId: $likeItemContentId")
        }
    }

    /*  suspend fun gettingUserScheduleIdList(userDocId : String): List<String>{

      }*/
    // 유저 컬렉션에 스케줄 서브리스트에 스케줄 문서 아이디 추가하기
    suspend fun addTripScheduleToUserSubCollection(
        userDocId: String,
        tripScheduleDocId: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val userScheduleRef = firestore.collection("UserData")
            .document(userDocId)
            .collection("UserScheduleData")

        // 새로운 문서를 자동 ID로 추가하고, 필드에는 userDocId와 tripScheduleDocId 저장
        val scheduleData = mapOf(
            "userDocId" to userDocId,
            "tripScheduleDocId" to tripScheduleDocId
        )

        userScheduleRef.add(scheduleData).await()
    }

    // 유저컬렉션에서 스케줄 서브컬렉션의 스케줄 문서 아이디 가져오기
    suspend fun gettingTripScheduleItemList(userDocId: String): List<String> {
        val result = mutableListOf<String>()
        val firestore = FirebaseFirestore.getInstance()
        val userScheduleRef = firestore.collection("UserData")
            .document(userDocId)
            .collection("UserScheduleData")

        val snapshot = userScheduleRef.get().await()

        for (document in snapshot.documents) {
            val tripScheduleDocId = document.getString("tripScheduleDocId")
            if (tripScheduleDocId != null) {
                result.add(tripScheduleDocId)
            }
        }

        return result
    }

    // 유저컬렉션에서 스케줄 서브컬렉션의 스케줄 문서 삭제
    suspend fun deleteTripScheduleItem(userDocId: String, tripScheduleDocId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userScheduleRef = firestore.collection("UserData")
            .document(userDocId)
            .collection("UserScheduleData")

        // tripScheduleDocId 필드 값이 인자로 받은 값과 같은 문서들 조회
        val querySnapshot = userScheduleRef
            .whereEqualTo("tripScheduleDocId", tripScheduleDocId)
            .get()
            .await()

        // 조건에 맞는 문서 삭제
        for (document in querySnapshot.documents) {
            userScheduleRef.document(document.id).delete().await()
        }
    }

    // 유저 좋아요 목록 플로우 가져오기
    fun getUserLikeListFlow(userDocId: String): Flow<List<String>> = callbackFlow {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore
            .collection("UserData")
            .document(userDocId)
            .collection("UserLikeList")

        val listenerRegistration = collectionReference.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("getUserLikeListFlow", "에러 발생", exception)
                trySend(emptyList())  // 에러 시 빈 리스트 보내기
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val likeList = snapshot.documents.mapNotNull { it.getString("contentId") }
                Log.d("getUserLikeListFlow", "Flow로 받은 contentId 리스트: $likeList")
                trySend(likeList)
            }
        }

        // flow가 종료되면 리스너 정리
        awaitClose {
            listenerRegistration.remove()
        }
    }
}

