package com.lion.wandertrip.service

import android.util.Log
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.repository.TripLocationBasedItemRepository

class TripLocationBasedItemService(val repository: TripLocationBasedItemRepository) {

    suspend fun gettingTripLocationBasedItemList(
        lat: String,
        lng: String,
        contentTypeId: String,
        page: Int = 1,
        radius: String,
        numOfRows : Int = 10
    ): MutableList<TripLocationBasedItem> {
        // 서비스에서 데이터를 요청하는 메소드
        return try {
            // TripCommonItemRepository의 메소드를 호출하여 데이터를 가져옵니다.
            val tripLocationBasedItemList = repository.gettingTripLocationBased(lat,lng,contentTypeId,page,radius,numOfRows)
            // 데이터를 반환
            tripLocationBasedItemList
        } catch (e: Exception) {
            // 예외가 발생한 경우 처리
            Log.e("gettingTripLocationBasedItemList", "Error occurred while getting trip common item", e)
            mutableListOf<TripLocationBasedItem>()
        }
    }
}