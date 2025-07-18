package com.lion.wandertrip.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lion.wandertrip.vo.ReviewVO
import kotlinx.coroutines.tasks.await
import java.io.File

class ContentsReviewRepository {

    // 사용자의 리뷰 문서 가져오기
    // 중앙 컬렉션에서 사용자 리뷰만 가져오기
    suspend fun getContentsUserReviewByNickName(contentsWriterNickName: String): List<ReviewVO> {
        return try {
            val db = FirebaseFirestore.getInstance()

            val reviewsSnapshot = db.collection("ContentsReview")
                .whereEqualTo("reviewWriterNickname", contentsWriterNickName)
                .get()
                .await()

            val reviews = reviewsSnapshot.documents.mapNotNull { it.toObject(ReviewVO::class.java) }

            reviews
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching reviews: ${e.message}", e)
            emptyList()
        }
    }



    // 리뷰 문서 1개 가져오기
    suspend fun getContentsReviewByDocId(contentsReviewDocId: String): ReviewVO {
        return try {
            Log.d("FirestoreDebug", "리뷰 문서를 가져오는 중: contentsReviewDocId = $contentsReviewDocId")

            val db = FirebaseFirestore.getInstance()
            val document = db.collection("ContentsReview")
                .document(contentsReviewDocId)
                .get()
                .await()

            if (document.exists()) {
                Log.d("FirestoreDebug", "문서 찾음: ${document.id}")
                val review = document.toObject(ReviewVO::class.java)
                Log.d("FirestoreDebug", "리뷰 데이터: $review")
                review ?: ReviewVO()
            } else {
                Log.d("FirestoreDebug", "문서가 존재하지 않음.")
                ReviewVO()
            }
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "리뷰 문서 가져오기 오류", e)
            ReviewVO()
        }
    }

    // 컨텐츠 모든 리뷰 가져오기
    suspend fun getAllReviewsWithContents(contentId: String): MutableList<ReviewVO> {
        val reviewList = mutableListOf<ReviewVO>()

        try {
            val db = FirebaseFirestore.getInstance()

            val reviewsQuerySnapshot = db.collection("ContentsReview")
                .whereEqualTo("contentId", contentId)
                .get()
                .await()

            for (reviewDoc in reviewsQuerySnapshot.documents) {
                val reviewVO = reviewDoc.toObject(ReviewVO::class.java)
                if (reviewVO != null) {
                    reviewList.add(reviewVO)
                } else {
                    Log.w("test100", "리뷰 변환 실패: ${reviewDoc.id}")
                }
            }

            Log.d("test100", "리뷰 ${reviewList.size}개 가져옴 (contentId=$contentId)")

        } catch (e: Exception) {
            Log.e("test100", "리뷰 가져오기 실패 (contentId=$contentId)", e)
        }

        return reviewList
    }


    // 리뷰 등록
    suspend fun addContentsReview(reviewVO: ReviewVO): String {
        try {
            val db = FirebaseFirestore.getInstance()
            val contentsRef = db.collection("ContentsReview")

            // 리뷰 추가
            val document = contentsRef.document()
            reviewVO.reviewDocId = document.id
            document.set(reviewVO).await()

            // Log.d("test100", "ContentsReviewRepository -> addContentsReview 리뷰 등록 성공: ${reviewRef.id} (ContentsID: $contentsId)")

            return document.id
        } catch (e: Exception) {
            Log.e("test100", "리뷰 등록 실패: ", e)
            return ""
        }
    }

// 리뷰 수정
suspend fun modifyContentsReview(reviewVO: ReviewVO): Boolean {
    return try {
        Log.d("ContentsReviewRepository", "수정할 리뷰: reviewDocId = ${reviewVO.reviewDocId}, contentId = ${reviewVO.contentId}, 내용 = ${reviewVO.reviewContent}")

        // 🔥 리뷰 문서 ID 검증 (빈 값이면 오류 방지)
        if (reviewVO.reviewDocId.isNullOrEmpty()) {
            Log.e("ContentsReviewRepository", "리뷰 문서 ID가 없음! reviewDocId = ${reviewVO.reviewDocId}")
            return false
        }

        // Firestore 인스턴스에서 바로 'ContentsReview' 컬렉션 접근
        val db = FirebaseFirestore.getInstance()
        val reviewRef = db.collection("ContentsReview").document(reviewVO.reviewDocId)

        // 문서 덮어쓰기 (수정)
        reviewRef.set(reviewVO).await()
        Log.d("ContentsReviewRepository", "리뷰 수정 성공: ${reviewVO.reviewDocId}")

        true
    } catch (e: Exception) {
        Log.e("ContentsReviewRepository", "리뷰 수정 실패: ${reviewVO.reviewDocId}", e)
        false
    }
}



        //닉네임 바꿀 때 사용하기
        // 닉변 전 게시물의 닉네임을 변경한 닉네임으로 update
        suspend fun changeReviewNickName(oldNickName: String, newNickName: String) {
            val firestore = FirebaseFirestore.getInstance()
            val reviewCollection = firestore.collection("ContentsReview")

            try {
                Log.d("test100", "📌 닉네임 변경 시작: $oldNickName → $newNickName")

                // 1. reviewWriterNickname이 oldNickName인 리뷰 문서들 찾기
                val snapshot = reviewCollection
                    .whereEqualTo("reviewWriterNickname", oldNickName)
                    .get()
                    .await()

                // 2. 각 문서에 대해 reviewWriterNickname 필드만 업데이트
                for (doc in snapshot.documents) {
                    val docId = doc.id
                    Log.d("test100", "🔄 닉네임 변경할 리뷰 ID: $docId")

                    reviewCollection.document(docId)
                        .update("reviewWriterNickname", newNickName)
                        .await()

                    Log.d("test100", "✅ 닉네임 변경 완료: $docId")
                }

                Log.d("test100", "🎉 전체 닉네임 변경 완료: $oldNickName → $newNickName")

            } catch (e: Exception) {
                Log.e("test100", "❌ 닉네임 변경 실패: $oldNickName → $newNickName", e)
            }
        }



    // 이미지 데이터를 서버로 업로드 하는 메서드
    suspend fun uploadReviewImageList(
        sourceFilePath: List<String>, // 업로드할 이미지 파일 경로 목록
        serverFilePath: List<String>, // 서버에 저장될 파일 이름 목록
        contentsId: String // 해당 콘텐츠의 ID
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
            val childReference = firebaseStorage.reference.child("contentsReviewImage/$contentsId/${serverFilePath[i]}")

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


    // 이미지 Uri 가져온다.
    // 이미지 Uri 리스트를 가져오는 함수
    suspend fun gettingReviewImageList(imageFileNameList: List<String>, contentsId: String): List<Uri> {
        Log.d("gettingImage", "이미지 파일명을 받음: ${imageFileNameList.joinToString()}")

        val storageReference = FirebaseStorage.getInstance().reference
        Log.d("gettingImage", "Firebase Storage 레퍼런스 초기화됨")

        return try {
            // 각 파일명에 대해 URI를 가져오는 작업을 비동기 처리
            val uriList = imageFileNameList.map { fileName ->
                val childStorageReference = storageReference.child("contentsReviewImage/$contentsId/$fileName")
                Log.d("gettingImage", "이미지 파일 경로: contentsReviewImage$contentsId/$fileName")
                childStorageReference.downloadUrl.await()  // 개별적으로 URI 가져오기
            }

            Log.d("gettingImage", "이미지 URI 리스트 가져옴: ${uriList.joinToString()}")
            uriList
        } catch (e: Exception) {
            Log.e("gettingImage", "이미지 URI 가져오기 실패: ${e.message}")
            emptyList()
        }
    }

    // 삭제 메서드
    suspend fun deleteContentsReview(contentsReviewDocId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()

            // 🔥 최상위 컬렉션에서 리뷰 문서 삭제
            db.collection("ContentsReview")
                .document(contentsReviewDocId)
                .delete()
                .await()

            // 🔥 관련 이미지 삭제 (경로에 contentsDocId가 포함되지 않는다고 가정)
            val imageRef = storage.reference.child("reviews/$contentsReviewDocId.jpg")
            imageRef.delete().await()

            Log.d("Firestore", "✅ 리뷰 및 이미지 삭제 성공: $contentsReviewDocId")

        } catch (e: Exception) {
            Log.e("Firestore", "❌ 리뷰 또는 이미지 삭제 실패: $contentsReviewDocId", e)
        }
    }


}