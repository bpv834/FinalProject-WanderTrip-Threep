package com.lion.wandertrip.repository

import android.util.Log
import com.lion.wandertrip.model.TripCommonItem
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.retrofit_for_practice.TripKeywordItemInterface

class TripKeywordItemRepository(private val api: TripKeywordItemInterface) {
    private val myKey =
        "qe8EY3d1ixNdu4/lC0aXJXbTH/VndGcoj5DABUigtfSCLIIP48IHXbwMEkG5gkGvVW/wKl1XuuFyqYwwWQZJDg=="

    // 키워드로 tripItem 가져오기
    suspend fun gettingTripItemByKeyword(keyword: String): TripItemModel? {
        return try {
            val response = api.getKeywordTripItem(
                numOfRows = 10,
                pageNo = 1,
                mobileOS = "ETC",
                mobileApp = "WanderTrip",
                type = "json",
                listYN = "Y",
                arrange = "Q",
                keyword = keyword,
                serviceKey = myKey
            )

            if (response.isSuccessful) {
                val items = response.body()?.response?.body?.items?.item.orEmpty()

                // contentTypeId == "12" (관광지)인 첫 번째 아이템 선택
                val item = items.firstOrNull { it.contentTypeId == "12" }?.let {
                    TripItemModel(
                        contentId = it.contentId ?: "",
                        contentTypeId = it.contentTypeId ?: "",
                        title = it.title ?: "",
                        tel = it.tel ?: "",
                        firstImage = it.firstImage ?: "",
                        areaCode = it.areaCode ?: "",
                        addr1 = it.addr1 ?: "",
                        addr2 = it.addr2 ?: "",
                        mapLat = it.mapLat?.toDouble() ?: 0.0,
                        mapLong = it.mapLng?.toDouble() ?: 0.0
                    )
                }

                return item
            } else {
                Log.e("TripRepo", "API request failed: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TripRepo", "Error occurred while fetching trip item", e)
            null
        }
    }
}
