package com.lion.wandertrip.repository

import android.util.Log
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.retrofit_for_practice.TripKeywordItemInterface
import com.lion.wandertrip.util.ContentTypeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class TripKeywordItemRepository(private val api: TripKeywordItemInterface) {
    private val myKey =
        "qe8EY3d1ixNdu4/lC0aXJXbTH/VndGcoj5DABUigtfSCLIIP48IHXbwMEkG5gkGvVW/wKl1XuuFyqYwwWQZJDg=="

    // 키워드로 tripItem 가져오기
    suspend fun gettingTripItemByKeyword(keyword: String): TripItemModel? {
   //     Log.d("TripKeywordItemRepository", "gettingTripItemByKeyword - keyword: $keyword")
        return try {
            Log.d("TripKeywordItemRepository","1 : gettingTripItemByKeyword")

            val response = api.getKeywordTripItem(
                numOfRows = 20,
                pageNo = 1,
                mobileOS = "ETC",
                mobileApp = "WanderTrip",
                type = "json",
                arrange = "O",
                keyword = keyword,
                serviceKey = myKey
            )
            Log.d("TripRepo", "raw response: ${response.body()}")
            Log.d("TripRepo", "error body: ${response.errorBody()?.string()}")
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody == null) {
                    Log.e("TripRepo", "응답 본문이 null입니다.")
                    return null
                }

                val items = responseBody.response?.body?.items?.item.orEmpty()

                if (items.isEmpty()) {
                    Log.w("TripRepo", "검색된 아이템이 없습니다.")
                    return null
                }

                val item = items.firstOrNull { it.contentTypeId == "12" }?.let {
                    TripItemModel(
                        contentId = it.contentId ?: "",
                        contentTypeId = it.contentTypeId ?: "",
                        title = it.title ?: "",
                        tel = it.tel ?: "",
                        firstImage = it.firstImage ?: "",
                        areaCode = it.areaCode ?: "",
                        sigunguCode = it.siGunGuCode ?: "",
                        addr1 = it.addr1 ?: "",
                        addr2 = it.addr2 ?: "",
                        mapLat = it.mapLat?.toDouble() ?: 0.0,
                        mapLong = it.mapLng?.toDouble() ?: 0.0
                    )
                }

                if (item == null) {
                    Log.w("TripRepo", "contentTypeId == \"12\" 조건에 맞는 아이템이 없습니다.")
                }

                return item
            } else {
                Log.e(
                    "TripRepo",
                    "API 요청 실패 - Code: ${response.code()}, Message: ${response.message()}"
                )
                null
            }
        } catch (e: Exception) {
            Log.e("TripRepo", "여행지 정보 요청 중 예외 발생${e.message}")
            null
        }
    }

    // 키워드로 tripItem 가져오기
    suspend fun gettingTripItemAllByKeyword(keyword: String): List<TripItemModel> {
       // Log.d("TripKeywordItemRepository", "gettingTripItemByKeyword - keyword: $keyword")
        Log.d("TripKeywordItemRepository","2 : gettingTripItemAllByKeyword")

        return try {

            val response = api.getKeywordTripItem(
                numOfRows = 1000,
                pageNo = 1,
                mobileOS = "ETC",
                mobileApp = "WanderTrip",
                type = "json",
                arrange = "O",
                keyword = keyword,
                serviceKey = myKey,
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody == null) {
                    Log.e("TripRepo", "응답 본문이 null입니다.")
                    return emptyList()
                }

                val items = responseBody.response?.body?.items?.item.orEmpty()

                if (items.isEmpty()) {
                    Log.w("TripRepo", "검색된 아이템이 없습니다.")
                    return emptyList()
                }

                return items
                    .filter {
                        it.contentTypeId == ContentTypeId.TOURIST_ATTRACTION.contentTypeCode.toString()
                                || it.contentId == ContentTypeId.ACCOMMODATION.contentTypeCode.toString()
                                || it.contentId == ContentTypeId.RESTAURANT.contentTypeCode.toString()
                    }
                    .map {
                        TripItemModel(
                            contentId = it.contentId ?: "",
                            contentTypeId = it.contentTypeId ?: "",
                            title = it.title ?: "",
                            tel = it.tel ?: "",
                            firstImage = it.firstImage ?: "",
                            areaCode = it.areaCode ?: "",
                            sigunguCode = it.siGunGuCode ?: "",
                            addr1 = it.addr1 ?: "",
                            addr2 = it.addr2 ?: "",
                            mapLat = it.mapLat?.toDouble() ?: 0.0,
                            mapLong = it.mapLng?.toDouble() ?: 0.0,
                            cat2 = it.cat2.toString(),
                            cat3 = it.cat3.toString()
                        )
                    }
            } else {
                Log.e(
                    "TripRepo",
                    "API 요청 실패 - Code: ${response.code()}, Message: ${response.message()}"
                )
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TripRepo", "여행지 정보 요청 중 예외 발생 ${e.message}")
            emptyList()
        }
    }

}
