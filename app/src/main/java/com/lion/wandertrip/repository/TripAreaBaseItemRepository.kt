package com.lion.wandertrip.repository

import android.util.Log
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.retrofit_for_practice.TripAreaBaseItemInterface

class TripAreaBaseItemRepository(private val api: TripAreaBaseItemInterface) {
    val myKey =
        "qe8EY3d1ixNdu4/lC0aXJXbTH/VndGcoj5DABUigtfSCLIIP48IHXbwMEkG5gkGvVW/wKl1XuuFyqYwwWQZJDg=="

    suspend fun gettingAreaBaseTourItems(): List<TripItemModel>? {
        return try {
            val response = api.getAreaBaseTripItem(
                serviceKey = myKey,
                mobileOS = "ETC",
                mobileApp = "com.lion.wandertrip",
                type = "json",
                numOfRows = 50000,
                pageNo = 1
            )

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val validContentTypes = setOf(12, 32, 39) // 🔥 가져올 ContentTypeId 목록

                    val items = apiResponse.response.body.items.item
                        .filter { it.contentTypeId?.toIntOrNull() in validContentTypes } // 🔥 필터링
                        .shuffled()
                        .take(3) // 🔥 무작위로 3개 선택

                    items.map {
                        TripItemModel(
                            contentId = it.contentId ?: "",
                            contentTypeId = it.contentTypeId ?: "",
                            title = it.title ?: "",
                            tel = it.tel ?: "",
                            firstImage = it.firstImage ?: "",
                            areaCode = it.areaCode ?: "",
                            addr1 = it.addr1 ?: "",
                            addr2 = it.addr2 ?: "",
                            mapLat = it.mapLat?.toDoubleOrNull() ?: 0.0,
                            mapLong = it.mapLng?.toDoubleOrNull() ?: 0.0,
                            cat2 = it.cat2 ?: "",
                            cat3 = it.cat3 ?: ""
                        )
                    }
                }
            } else {
                Log.d("API_ERROR", "API request failed: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error occurred while fetching tour items", e)
            null
        }
    }


    suspend fun gettingAllItem(): List<TripItemModel>? {
        return try {
            val response = api.getAreaBaseTripItem(
                serviceKey = myKey,
                mobileOS = "ETC",
                mobileApp = "com.lion.wandertrip",
                type = "json",
                numOfRows = 50000,
                pageNo = 1
            )

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val items =
                        apiResponse.response.body.items.item
                    items.map {
                        TripItemModel(
                            contentId = it.contentId ?: "",
                            contentTypeId = it.contentTypeId ?: "",
                            title = it.title ?: "",
                            tel = it.tel ?: "",
                            firstImage = it.firstImage ?: "",
                            areaCode = it.areaCode ?: "",
                            addr1 = it.addr1 ?: "",
                            addr2 = it.addr2 ?: "",
                            mapLat = it.mapLat?.toDoubleOrNull() ?: 0.0, // 🔥 문자열을 Double로 변환
                            mapLong = it.mapLng?.toDoubleOrNull()
                                ?: 0.0, // 🔥 mapLng → mapLong으로 매칭
                            cat2 = it.cat2 ?: "",
                            cat3 = it.cat3 ?: ""
                        )
                    }
                }
            } else {
                Log.d("API_ERROR", "API request failed: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error occurred while fetching tour items", e)
            null
        }
    }
}
