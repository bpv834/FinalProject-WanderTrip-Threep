package com.lion.wandertrip.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lion.wandertrip.retrofit.ApiResponse
import com.lion.wandertrip.retrofit.RetrofitClient
import com.lion.wandertrip.vo.ScheduleItemVO
import com.lion.wandertrip.vo.TripItemVO
import com.lion.wandertrip.vo.TripScheduleVO
import com.lion.wandertrip.vo.UserVO
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class TripScheduleRepository {

    // 일정을 추가 하는 메서드
    suspend fun addTripSchedule(tripScheduleVO: TripScheduleVO) : String {
        val firestore = FirebaseFirestore.getInstance()
        val collectionReference = firestore.collection("TripSchedule")

        // 1) Firestore에서 자동 생성할 문서 레퍼런스 획득
        val docRef = collectionReference.document()  // 자동 생성된 문서 ID가 docRef.id에 담김

        // 2) 문서 ID를 VO에 저장
        tripScheduleVO.tripScheduleDocId = docRef.id

        // 3) Firestore에 저장 (코루틴을 쓰므로 .await() 사용)
        docRef.set(tripScheduleVO).await()

        return docRef.id
    }

    // 일정 문서 id를 유저 일정 리스트에 추가
    suspend fun addTripDocIdToUserScheduleList(userDocId: String, tripScheduleDocId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("UserData").document(userDocId)
        // userScheduleList 필드에 tripScheduleDocId 추가 (중복 시 추가되지 않음)
        userDocRef.update("userScheduleList", FieldValue.arrayUnion(tripScheduleDocId)).await()
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


    // ✅ 일정에 여행지 항목 추가 함수
    suspend fun addTripItemToSchedule(docId: String, scheduleDate: Timestamp, scheduleItemVO: ScheduleItemVO) {
        val firestore = FirebaseFirestore.getInstance()
        val subCollectionRef = firestore.collection("TripSchedule")
            .document(docId)
            .collection("TripScheduleItem")

        try {
            // ✅ Firestore에서 scheduleDate와 동일한 날짜를 가진 항목 가져오기
            val snapshot = subCollectionRef.whereEqualTo("itemDate", scheduleDate).get().await()

            // ✅ 동일한 날짜를 가진 항목 중 가장 높은 itemIndex 찾기
            val maxIndex = snapshot.documents
                .mapNotNull { it.getLong("itemIndex")?.toInt() } // 🔹 Long → Int 변환
                .maxOrNull() ?: 0 // 값이 없으면 기본값 0

            // ✅ 새로운 itemIndex 설정 (최소 1부터 시작)
            val newItemIndex = if (maxIndex == 0) 1 else maxIndex + 1

            // ✅ Firestore에 새로운 아이템 추가
            val newItemRef = subCollectionRef.document()
            scheduleItemVO.itemDocId = newItemRef.id // Firestore 문서 ID 설정
            scheduleItemVO.itemIndex = newItemIndex // 새로운 인덱스 설정

            // ✅ Firestore에 저장
            newItemRef.set(scheduleItemVO).await()

            println("새로운 여행지 추가 완료: ${scheduleItemVO.itemTitle} (index: $newItemIndex)")
        } catch (e: Exception) {
            println("Firestore 추가 실패: ${e.message}")
        }
    }

    // 일정 항목 삭제 후 itemIndex 재조정
    suspend fun removeTripScheduleItem(scheduleDocId: String, itemDocId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val subCollectionRef = firestore.collection("TripSchedule")
            .document(scheduleDocId)
            .collection("TripScheduleItem")

        Log.d("TripSchedule", "Attempting to delete item: $itemDocId from schedule: $scheduleDocId")

        // 삭제할 문서의 스냅샷을 가져와 itemIndex와 itemDate를 확인합니다.
        val docSnapshot = subCollectionRef.document(itemDocId).get().await()
        if (!docSnapshot.exists()) {
            Log.w("TripSchedule", "Item document $itemDocId does not exist.")
            return
        }

        val deletedIndex = docSnapshot.getLong("itemIndex")?.toInt()
        val deletedItemDate = docSnapshot.getTimestamp("itemDate")


        if (deletedIndex == null || deletedItemDate == null) {
            Log.w("TripSchedule", "Missing itemIndex or itemDate for document $itemDocId")
            return
        }

        Log.d("TripSchedule", "Deleting item with index $deletedIndex and date $deletedItemDate")

        // 해당 문서를 삭제합니다.
        subCollectionRef.document(itemDocId).delete().await()
        Log.d("TripSchedule", "Deleted item document $itemDocId")

        // 삭제한 문서와 동일한 itemDate를 가진, itemIndex가 삭제된 값보다 큰 모든 문서를 조회합니다.
        val querySnapshot = subCollectionRef
            .whereEqualTo("itemDate", deletedItemDate)
            .whereGreaterThan("itemIndex", deletedIndex)
            .get()
            .await()

        Log.d("TripSchedule", "Found ${querySnapshot.size()} items to update indices")

        // 각 문서의 itemIndex를 1씩 감소시켜 재조정합니다.
        for (doc in querySnapshot.documents) {
            val currentIndex = doc.getLong("itemIndex")?.toInt() ?: continue
            val newIndex = currentIndex - 1
            subCollectionRef.document(doc.id).update("itemIndex", newIndex).await()
            Log.d("TripSchedule", "Updated document ${doc.id} index: $currentIndex -> $newIndex")
        }

        Log.d("TripSchedule", "Item index adjustment completed after deletion.")
    }


    // 일정 항목 문서 id로 일정 항목 가져 오기
    suspend fun getScheduleItemByDocId(tripScheduleDocId: String, scheduleItemDocId: String,): ScheduleItemVO? {
        val firestore = FirebaseFirestore.getInstance()
        val subCollectionRef = firestore.collection("TripSchedule")
            .document(tripScheduleDocId)
            .collection("TripScheduleItem")

        return try {
            val docSnapshot = subCollectionRef
                .document(scheduleItemDocId)
                .get()
                .await()  // 코루틴 사용

            if (docSnapshot.exists()) {
                // Firestore 문서를 ScheduleItemVO로 매핑
                docSnapshot.toObject(ScheduleItemVO::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 일정 항목 업데이트
    suspend fun updateScheduleItem(
        tripScheduleDocId: String,
        scheduleItemDocId: String,
        updatedItem: ScheduleItemVO
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val subCollectionRef = firestore.collection("TripSchedule")
            .document(tripScheduleDocId)
            .collection("TripScheduleItem")

        try {
            // 해당 문서 참조
            val docRef = subCollectionRef.document(scheduleItemDocId)

            // Firestore update (부분 업데이트)
            docRef.update(
                mapOf(
                    "itemReviewImagesURL" to updatedItem.itemReviewImagesURL,
                    "itemReviewText" to updatedItem.itemReviewText
                )
            ).await()

            // 필요하다면 추가 작업(로그, 리턴 등)
        } catch (e: Exception) {
            e.printStackTrace()
            // 예외 처리 로직
        }
    }

    // 단일 Bitmap 업로드 -> 다운 로드 URL
    suspend fun uploadBitmapListToFirebase(bitmaps: List<Bitmap>): List<String> {
        val TAG = "FirebaseUpload"
        val downloadUrls = mutableListOf<String>()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://wandertripsole.firebasestorage.app")
            .child("scheduleItemImage")

        Log.d(TAG, "업로드 시작: 총 ${bitmaps.size}개의 이미지")

        for ((index, bitmap) in bitmaps.withIndex()) {
            try {
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val imageRef = storageRef.child(fileName)

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                Log.d(TAG, "[$index] $fileName 업로드 시작")
                imageRef.putBytes(data).await()
                val downloadUrl = imageRef.downloadUrl.await()
                downloadUrls.add(downloadUrl.toString())

                Log.d(TAG, "[$index] $fileName 업로드 성공: $downloadUrl")
            } catch (e: Exception) {
                Log.e(TAG, "[$index] 이미지 업로드 실패: ${e.message}", e)
            }
        }

        Log.d(TAG, "업로드 완료: 성공한 이미지 ${downloadUrls.size}개")
        return downloadUrls
    }


    // 위치 조정한 일정 항목 업데이트
    suspend fun updateItemsPosition(tripScheduleDocId: String, updatedItems: List<ScheduleItemVO>) {
        val firestore = FirebaseFirestore.getInstance()
        val subCollectionRef = firestore.collection("TripSchedule")
            .document(tripScheduleDocId)
            .collection("TripScheduleItem")

        val batch = firestore.batch()
        updatedItems.forEach { scheduleItem ->
            val docRef = subCollectionRef.document(scheduleItem.itemDocId)
            // Firestore 업데이트 시, itemIndex를 1부터 시작하는 값으로 업데이트
            batch.update(docRef, "itemIndex", scheduleItem.itemIndex)
        }
        batch.commit().await()
    }

    // 초대할 닉네임으로 유저 존재 여부 확인 후, 있으면 문서 ID 반환, 없으면 빈 문자열 반환
    suspend fun addInviteUserByInviteNickname(scheduleDocId: String, inviteNickname: String): String {
        val firestore = FirebaseFirestore.getInstance().collection("UserData")
        val querySnapshot = firestore
            .whereEqualTo("userNickName", inviteNickname)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            // 검색된 첫 번째 문서 업데이트
            val userDoc = querySnapshot.documents.first()
            userDoc.reference.update("invitedScheduleList", FieldValue.arrayUnion(scheduleDocId))
                .await()
            userDoc.id
        } else {
            ""
        }
    }

    // 초대한 유저 문서 Id를 데이터에 추가
    suspend fun addInviteUserDocIdToScheduleInviteList(scheduleDocId: String, invitedUserDocId: String): Boolean {
        val firestore = FirebaseFirestore.getInstance()
        val scheduleDocRef = firestore.collection("TripSchedule").document(scheduleDocId)

        val snapshot = scheduleDocRef.get().await()
        if (snapshot.exists()) {
            // scheduleInviteList 필드를 읽어옴 (없으면 빈 리스트)
            val inviteList = snapshot.get("scheduleInviteList") as? List<String> ?: emptyList()
            if (inviteList.contains(invitedUserDocId)) {
                // 이미 초대된 docId가 있으면 false 반환
                return false
            } else {
                // 초대되지 않은 경우, invitedUserDocId를 추가하고 true 반환
                scheduleDocRef.update("scheduleInviteList", FieldValue.arrayUnion(invitedUserDocId)).await()
                return true
            }
        } else {
            // 문서가 존재하지 않으면, 새로 생성하면서 초대 리스트에 추가하고 true 반환
            scheduleDocRef.set(mapOf("scheduleInviteList" to listOf(invitedUserDocId))).await()
            return true
        }
    }

    // 유저 DocId 리스트로 유저 정보 가져오기
    suspend fun fetchUserScheduleList(userDocIdList: List<String>): List<UserVO> {
        val firestore = FirebaseFirestore.getInstance()
        val userList = mutableListOf<UserVO>()
        // 안전한 반복을 위해 toList()로 복사
        for (docId in userDocIdList.toList()) {
            try {
                val snapshot = firestore.collection("UserData")
                    .document(docId)
                    .get()
                    .await()
                if (snapshot.exists()) {
                    val user = snapshot.toObject(UserVO::class.java)
                    user?.let { userList.add(it) }
                } else {
                    Log.d("fetchUserScheduleList", "Document $docId does not exist")
                }
            } catch (e: Exception) {
                Log.e("fetchUserScheduleList", "Error fetching user for docId: $docId", e)
            }
        }
        return userList
    }

    // 유저 일정 docId로 일정 항목 가져 오기
    suspend fun fetchScheduleList(userScheduleDocIdList: List<String>): List<TripScheduleVO> {
        val firestore = FirebaseFirestore.getInstance()
        val scheduleItemList = mutableListOf<TripScheduleVO>()
        for (docId in userScheduleDocIdList) {
            try {
                val snapshot = firestore.collection("TripSchedule")
                    .document(docId)
                    .get()
                    .await()
                if (snapshot.exists()) {
                    val item = snapshot.toObject(TripScheduleVO::class.java)
                    item?.let { scheduleItemList.add(it) }
                } else {
                    Log.d("fetchUserScheduleList", "Document $docId does not exist")
                }
            } catch (e: Exception) {
                Log.e("fetchUserScheduleList", "Error fetching docId: $docId", e)
            }
        }
        // scheduleTimeStamp 기준 내림차순 정렬 (최신순)
        return scheduleItemList.sortedByDescending { it.scheduleTimeStamp }
    }

    // 유저 일정 리스트에서 일정 삭제
    suspend fun removeUserScheduleList(userDocId: String, scheduleDocId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("UserData").document(userDocId)
        val subCollectionRef = userDocRef.collection("UserScheduleData")

        // tripScheduleDocId 값이 scheduleDocId와 같은 문서 쿼리
        val querySnapshot = subCollectionRef.whereEqualTo("tripScheduleDocId", scheduleDocId).get().await()

        for (document in querySnapshot.documents) {
            // 문서 삭제
            subCollectionRef.document(document.id).delete().await()
        }
    }

    // 초대 받은 일정 리스트에서 일정 삭제
    suspend fun removeInvitedScheduleList(userDocId: String, invitedScheduleDocId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("UserData").document(userDocId)

        val snapshot = userDocRef.get().await()
        if (!snapshot.exists()) return

        val scheduleList = snapshot.get("invitedScheduleList") as? List<String> ?: emptyList()
        if (scheduleList.contains(invitedScheduleDocId)) {
            userDocRef.update("invitedScheduleList", FieldValue.arrayRemove(invitedScheduleDocId)).await()
        }
    }

    // 일정에서 초대된 유저 문서 Id 삭제
    suspend fun removeScheduleInviteList(tripScheduleDocId: String, userDocId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val scheduleDocRef = firestore.collection("TripSchedule").document(tripScheduleDocId)

        // 문서가 존재하는지 확인 후, scheduleInviteList에서 userDocId 제거
        val snapshot = scheduleDocRef.get().await()
        if (snapshot.exists()) {
            scheduleDocRef.update("scheduleInviteList", FieldValue.arrayRemove(userDocId)).await()
        }
    }

    // 공공 데이터 관련 //////////////////////////////////////////////////////////////////////////////

    // API 호출 및 데이터 로드
    suspend fun loadTripItems(serviceKey: String, areaCode: String, contentTypeId: String) : List<TripItemVO>? {
        // ✅ TripItemModel 대신 TripItemVO 리스트 사용
        val tripItemList = mutableListOf<TripItemVO>()

        try {
            // ✅ API 호출 시작 시간
            val apiStartTime = System.currentTimeMillis()

            val rawResponse = RetrofitClient.apiService.getItems(
                serviceKey = serviceKey,
                numOfRows = 10000,
                pageNo = 1,
                mobileOS = "AND",
                mobileApp = "WanderTrip",
                type = "json",
                showflag = "1",
                listYN = "Y",
                arrange = "A",
                contentTypeId = contentTypeId,
                areaCode = areaCode,
            )

            // ✅ API 응답 완료 시간 및 소요 시간 계산
            val apiEndTime = System.currentTimeMillis()
            val apiDuration = apiEndTime - apiStartTime
            Log.d("API_RESPONSE_TIME", "API 응답 소요 시간: ${apiDuration}ms")

            // 🚀 응답 로그 출력
            Log.d("APIResponseRaw", "Response: $rawResponse")

            // JSON 파싱
            val apiResponse = Json.decodeFromString<ApiResponse>(rawResponse)
            val items = apiResponse.response.body?.items?.item ?: emptyList()

            // ✅ 변환을 TripItemVO 내부에서 처리
            val tripItemVOs = items.map { TripItemVO.from(it) }


            tripItemList.clear()
            tripItemList.addAll(tripItemVOs)

            Log.d("APIProcessedData", "총 데이터 개수: ${tripItemList.size}")

        } catch (e: Exception) {
            Log.e("APIError", "API 호출 오류: ${e.message}")
        }
        return tripItemList
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // hj
    // 내 여행 목록 가져오기
    suspend fun gettingMyTripSchedules(userNickName: String): MutableList<TripScheduleVO> {
        val firestore = FirebaseFirestore.getInstance()
        val collRef = firestore.collection("TripSchedule")

        val tripSchedules = mutableListOf<TripScheduleVO>()

        try {
            // userID가 일치하는 문서를 가져오기 위한 쿼리
            val querySnapshot = collRef.whereEqualTo("userNickName", userNickName).get().await()

            // 가져온 문서를 TripScheduleVO로 변환하여 리스트에 추가
            for (document in querySnapshot.documents) {
                val tripSchedule = document.toObject(TripScheduleVO::class.java)
                if (tripSchedule != null) {
                    tripSchedules.add(tripSchedule)
                }
            }

            // 쿼리 결과 로그 출력 (디버그용)
            Log.d("test100", "userID: $userNickName")

        } catch (e: Exception) {
            // 예외가 발생하면 에러 메시지 로그 출력
            Log.e("test100", "에러남: $userNickName, $e", e)
        }

        // 결과 반환
        return tripSchedules
    }
    // hj
    //닉네임 바꿀 때 사용하기
    // 닉변 전 게시물의 닉네임을 변경한 닉네임으로 update
    suspend fun changeTripScheduleNickName(oldNickName: String, newNickName: String) {
        val firestore = FirebaseFirestore.getInstance()
        val collRef = firestore.collection("TripSchedule")

        try {
            val querySnapshot = collRef.whereEqualTo("userNickName", oldNickName).get().await()

            if (querySnapshot.isEmpty) {
                Log.d("test100", "변경할 닉네임($oldNickName)이 존재하지 않습니다.")
                return
            }

            for (document in querySnapshot.documents) {
                val docRef = collRef.document(document.id)
                docRef.update("userNickName", newNickName).await()
            }
        } catch (e: Exception) {
            Log.e("test100", "닉네임 변경 중 오류 발생: $e", e)
        }
    }
    // hj
    // 여행 삭제
    suspend fun deleteTripScheduleByDocId(docId : String) {
        val firestore = FirebaseFirestore.getInstance()
        val collRef = firestore.collection("TripSchedule")

        try {
            val querySnapshot = collRef.whereEqualTo("tripScheduleDocId", docId).get().await()

            if (querySnapshot.isEmpty) {
                Log.d("test100", "($docId)이 존재하지 않습니다.")
                return
            }

            for (document in querySnapshot.documents) {
                val docRef = collRef.document(document.id)
                docRef.delete().await()
            }
        } catch (e: Exception) {
            Log.e("test100", "닉네임 변경 중 오류 발생: $e", e)
        }
    }


}