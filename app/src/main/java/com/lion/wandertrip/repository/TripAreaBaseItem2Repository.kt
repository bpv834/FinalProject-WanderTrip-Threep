package com.lion.wandertrip.repository

import android.util.Log
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.retrofit_for_practice.TripAreaBaseItem2Interface

class TripAreaBaseItem2Repository(private val api: TripAreaBaseItem2Interface) {
    val myKey =
        "qe8EY3d1ixNdu4/lC0aXJXbTH/VndGcoj5DABUigtfSCLIIP48IHXbwMEkG5gkGvVW/wKl1XuuFyqYwwWQZJDg=="
    suspend fun gettingAllItemWithAreaCode(areaCode: String, sigunguCode: String?): List<TripItemModel>? {
        return try {
            Log.d("API_CALL", "요청 시작 - areaCode: $areaCode, sigunguCode: $sigunguCode")

            if(areaCode == ""&&sigunguCode == "") return emptyList<TripItemModel>()
            val response = api.getAreaBasedList(
                serviceKey = myKey,
                mobileOS = "ETC",
                mobileApp = "com.lion.wandertrip",
                type = "json",
                numOfRows = 10000,
                pageNo = 1,
                areaCode = areaCode,
                sigunguCode = sigunguCode,
                arrange = "O"
            )

            if (response.isSuccessful) {
                Log.d("API_CALL", "응답 성공 - code: ${response.code()}")

                response.body()?.let { apiResponse ->
                    val items = apiResponse.response.body.items.item
                    Log.d("API_CALL", "받은 item 개수: ${items.size}")

                    val convertedList = items.map {
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
                            mapLat = it.mapLat?.toDoubleOrNull() ?: 0.0,
                            mapLong = it.mapLng?.toDoubleOrNull() ?: 0.0,
                            cat2 = it.cat2 ?: "",
                            cat3 = it.cat3 ?: ""
                        )
                    }

                    Log.d("API_CALL", "TripItemModel 변환 완료 - 반환 예정")
                    convertedList
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