package com.lion.wandertrip.service

import android.util.Log
import com.lion.wandertrip.model.TripCommonItem
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.repository.TripAreaBaseItemRepository

class TripAreaBaseItemService(private val repository: TripAreaBaseItemRepository) {

/*    suspend fun getTripAreaBaseItem(): List<TripItemModel>? {
        return try {
            val tripItem = repository.gettingAreaBaseTourItems()
            // 데이터를 반환
            tripItem
        } catch (e: Exception) {
            // 예외가 발생한 경우 처리
            Log.e("TripCommonItemService", "Error occurred while getting trip common item", e)
            null
        }
    }*/

/*    suspend fun getTripAllItem(): List<TripItemModel>? {
        return try {
            val tripItem = repository.gettingAllItem()
            // 데이터를 반환
            tripItem
        } catch (e: Exception) {
            // 예외가 발생한 경우 처리
            Log.e("TripCommonItemService", "Error occurred while getting trip common item", e)
            null
        }
    }*/
}