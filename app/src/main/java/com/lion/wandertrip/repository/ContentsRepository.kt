package com.lion.wandertrip.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.lion.wandertrip.vo.ContentsVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class ContentsRepository {
    // 컨텐츠 가져오기
    suspend fun getContentByDocId(contentsDocId: String): ContentsVO {

        return try {
            val db = FirebaseFirestore.getInstance()
            val document = db.collection("ContentsData").document(contentsDocId).get().await()

            if (document.exists()) {
                document.toObject(ContentsVO::class.java) ?: ContentsVO()
            } else {
                ContentsVO()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ContentsVO() // 예외 발생 시 기본값 반환
        }
    }

    // 컨텐츠 가져오기 contentsID로
    suspend fun getContentByContentsId(contentsId: String): ContentsVO {
        return try {
            val db = FirebaseFirestore.getInstance()
            // contentId로 컨텐츠를 가져오기 위한 요청 로그
            Log.d("Firestore", "contentId로 컨텐츠 가져오기: $contentsId")

            // contentId 필드가 contentsId와 일치하는 문서를 필터링
            val documentSnapshot = db.collection("ContentsData")
                .whereEqualTo("contentId", contentsId)  // contentId 필드를 기준으로 필터링
                .get()
                .await()  // 비동기 처리로 결과를 기다림

            // 문서 스냅샷의 크기 출력 (몇 개의 문서가 반환되었는지 확인)
            Log.d("Firestore", "문서 스냅샷 조회 완료, 문서 크기: ${documentSnapshot.size()}")

            // 문서가 존재하는지 확인하고, 존재하면 해당 데이터를 반환
            if (!documentSnapshot.isEmpty) {
                val document = documentSnapshot.documents[0]
                // 문서가 존재하면 문서 ID 로그 출력
                Log.d("Firestore", "문서 찾음: ${document.id}")
                val content = document.toObject(ContentsVO::class.java) ?: ContentsVO()
                // 반환된 컨텐츠 객체 출력
                Log.d("Firestore", "컨텐츠 조회됨: $content")
                content
            } else {
                // 문서가 없을 경우 해당 로그 출력
                Log.d("Firestore", "contentId에 해당하는 문서가 없음: $contentsId")
                ContentsVO() // 문서가 없으면 기본값 반환
            }
        } catch (e: Exception) {
            // 예외 발생 시 오류 메시지와 함께 로그 출력
            Log.e("Firestore", "contentId로 컨텐츠 가져오기 중 오류 발생: $contentsId", e)
            e.printStackTrace()
            ContentsVO() // 예외 발생 시 기본값 반환
        }
    }

    // 컨텐츠 넣기
    suspend fun addContents(contentVO: ContentsVO): String {
        return try {
            val db = FirebaseFirestore.getInstance()
            val collection = db.collection("ContentsData")

            // 문서 ID 자동 생성
            val docRef = collection.document()
            contentVO.contentDocId = docRef.id

            // Firestore에 저장
            docRef.set(contentVO).await()

            Log.d("test100", "컨텐츠 추가 성공: ${contentVO.contentDocId}")

            docRef.id
        } catch (e: Exception) {
            Log.e("test100", "컨텐츠 추가 실패", e)
            ""
        }
    }

    // 컨텐츠 수정하기
    suspend fun modifyContents(contentVO: ContentsVO): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val collection = db.collection("ContentsData")

            // Firestore에 문서 덮어쓰기 (문서 ID 필수)
            val docRef = collection.document(contentVO.contentDocId)
            docRef.set(contentVO).await()

            Log.d("test100", "컨텐츠 덮어쓰기 성공: ${contentVO.contentDocId}")

            true
        } catch (e: Exception) {
            Log.e("test100", "컨텐츠 덮어쓰기 실패", e)
            false
        }
    }

    // 컨텐츠 존재 여부 확인 (존재하면 문서 ID 반환, 없으면 빈 문자열)
    suspend fun isContentExists(contentId: String): String? {
        return try {
            val db = FirebaseFirestore.getInstance()
            val querySnapshot = db.collection("ContentsData")
                .whereEqualTo("contentId", contentId) // contentId 필드 값이 같은 문서 찾기
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val documentId = querySnapshot.documents.first().id // 첫 번째 문서의 ID 가져오기
                Log.d("test100", "컨텐츠 존재 ($contentId): 문서 ID = $documentId")
                return documentId
            } else {
                Log.d("test100", "컨텐츠 없음: $contentId")
                return ""
            }
        } catch (e: Exception) {
            Log.e("test100", "컨텐츠 존재 여부 확인 실패: $contentId", e)
            null // 예외 발생 시 null 반환
        }
    }

    // 특정 컨텐츠의 리뷰 평점 평균을 계산하여 ContentsData 문서에 저장하고 리뷰 개수를 반환
    suspend fun updateContentRatingAndRatingCount(contentsDocId: String): Int {
        try {
            val db = FirebaseFirestore.getInstance()
            val contentsRef = db.collection("ContentsData").document(contentsDocId)
            val reviewsRef = contentsRef.collection("ContentsReview")

            // Log.d("test100", "updateContentRating ->평점 업데이트 시작: $contentsDocId")

            // 모든 리뷰 문서 가져오기
            val reviewsSnapshot = reviewsRef.get().await()
            val reviewCount = reviewsSnapshot.size() // 리뷰 개수 저장

            // Log.d("test100", "updateContentRating ->가져온 리뷰 개수: $reviewCount")

            // 리뷰가 없으면 ratingScore = 0으로 설정하고 리뷰 개수 반환
            if (reviewCount == 0) {
                Log.d("test100", "updateContentRating -> 리뷰 X: $contentsDocId, ratingScore = 0")
                contentsRef.update("ratingScore", 0).await()
                return 0
            }

            // 모든 리뷰의 ratingScore 값 가져오기
            val ratingList =
                reviewsSnapshot.documents.mapNotNull { it.getDouble("reviewRatingScore") }

            // Log.d("test100", "updateContentRating ->가져온 평점 리스트: $ratingList")

            // 평균 계산
            val avgRating = if (ratingList.isNotEmpty()) {
                ratingList.average()
            } else {
                0.0
            }

            // Log.d("test100", "계산된 평균 평점: $avgRating")

            // Firestore 문서 업데이트 (평균 평점)
            contentsRef.update("ratingScore", avgRating, "getRatingCount", reviewCount).await()
            // Log.d("test100", "컨텐츠 평점 업데이트 성공: $contentsDocId, 평균 평점: $avgRating, 리뷰 개수: $reviewCount")

            return reviewCount
        } catch (e: Exception) {
            Log.e("test100", "컨텐츠 평점 업데이트 실패: $contentsDocId", e)
            return -1 // 오류 발생 시 -1 반환
        }
    }

    // 특정 contentId에 해당하는 ContentsVO를 Flow로 반환
    // callbackFlow는 '변경 감지를 외부 리스너에서 하고', 그 값을 Flow로 흘려주는 역할을 해줌!"
    fun getContentsFlowByContentId(contentId: String): Flow<ContentsVO?> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val contentsCollection = db.collection("ContentsData")
        val query = contentsCollection
            .whereEqualTo("contentId", contentId)
            .limit(1)

        val registration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val contentsVO = snapshot.documents[0].toObject(ContentsVO::class.java)
                trySend(contentsVO) // VO 객체를 발행
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO) // callbackFlow 내부 코드가 IO 스레드에서 실행되도록 지정할 수 있음

    // 모든 ContentsVO 리스트를 Flow로 반환 (ViewModel에서 Map 구성을 위해 필요)
    fun getAllContentsFlow(): Flow<List<ContentsVO>> = callbackFlow {
        Log.d("FirestoreFlow", "모든 ContentsVO 리스트 실시간 구독 시작")
        val db = FirebaseFirestore.getInstance()
        val contentsCollection = db.collection("ContentsData")
        val registration = contentsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FirestoreFlow", "모든 ContentsVO 리스트 구독 오류", e)
                close(e) // 이 시점에서 채널이 닫히고 예외가 전파되므로, 더 이상 값을 보낼 필요가 없습니다.
                return@addSnapshotListener // 콜백 함수에서 즉시 반환하여 아래 코드가 실행되지 않게 함.
            }

            if (snapshot != null) {
                val voList = snapshot.documents.mapNotNull { document ->
                    document.toObject(ContentsVO::class.java)
                }
                Log.d("FirestoreFlow", "모든 ContentsVO 리스트 실시간 업데이트 (총 ${voList.size}개)")
                // trySend는 ChannelResult<Unit>를 반환.
                // Kotlin 1.5 이상에서는 .isSuccess 또는 .isFailure 등으로 결과 처리 가능.
                // 여기서는 단순히 값을 보내고 반환값은 무시합니다.
                trySend(voList).isSuccess // 명시적으로 반환값 무시 (또는 그냥 trySend(voList))
            } else {
                Log.w("FirestoreFlow", "Firestore 스냅샷이 null입니다. 빈 리스트 발행.")
                trySend(emptyList()).isSuccess // 명시적으로 반환값 무시 (또는 그냥 trySend(emptyList()))
            }
        }

        // awaitClose 블록은 Unit을 반환해야 합니다.
        // registration.remove()는 Unit을 반환하므로 문제가 없어야   합니다.
        awaitClose {
            Log.d("FirestoreFlow", "모든 ContentsVO 리스트 실시간 구독 취소")
            registration.remove() // 이 함수의 반환값이 Unit이므로 문제가 되지 않습니다.
        }
    }.flowOn(Dispatchers.IO)


}