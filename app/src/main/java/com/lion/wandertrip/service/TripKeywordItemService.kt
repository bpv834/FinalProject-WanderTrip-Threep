package com.lion.wandertrip.service

import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.repository.TripKeywordItemRepository

class TripKeywordItemService(val repository: TripKeywordItemRepository) {
    // 키워드로 tripItem 가져오기
    suspend fun gettingTripItemByKeyword(keyword: String): TripItemModel? {
        return repository.gettingTripItemByKeyword(keyword)
    }

    suspend fun gettingTripItemAllByKeyword(keyword: String): List<TripItemModel> {
        return repository.gettingTripItemAllByKeyword(keyword)
    }
}