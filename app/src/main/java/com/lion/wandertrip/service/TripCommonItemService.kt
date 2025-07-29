package com.lion.wandertrip.service

import android.util.Log
import com.lion.wandertrip.model.TripCommonItem
import com.lion.wandertrip.model.UserInterestingModel
import com.lion.wandertrip.repository.TripCommonItemRepository

class TripCommonItemService(private val repository: TripCommonItemRepository) {

    suspend fun getTripCommonItem(contentId: String, contentTypeId: String?): TripCommonItem? {
        // 서비스에서 데이터를 요청하는 메소드
        return try {
            // TripCommonItemRepository의 메소드를 호출하여 데이터를 가져옵니다.
            val tripItem = repository.gettingTripItemCommon(contentId, contentTypeId)
            // 데이터를 반환
            tripItem
        } catch (e: Exception) {
            // 예외가 발생한 경우 처리
            Log.e("TripCommonItemService", "Error occurred while getting trip common item", e)
            null
        }
    }

    // 사용자 관심 아이템 받아오기
    suspend fun gettingTripItemCommonInteresting(
        contentIdList: MutableList<String>,
    ): MutableList<UserInterestingModel> {
        return repository.gettingTripItemCommonInteresting(contentIdList)
    }

}