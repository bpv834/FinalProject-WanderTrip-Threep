package com.lion.wandertrip.repository

import android.util.Log
import com.lion.wandertrip.model.TripCommonItem
import com.lion.wandertrip.model.UserInterestingModel
import com.lion.wandertrip.retrofit_for_practice.TripCommonItemInterface

class TripCommonItemRepository(private val api: TripCommonItemInterface) {
    val myKey =
        "qe8EY3d1ixNdu4/lC0aXJXbTH/VndGcoj5DABUigtfSCLIIP48IHXbwMEkG5gkGvVW/wKl1XuuFyqYwwWQZJDg=="

    suspend fun gettingTripItemCommon(
        contentId: String, contentTypeId: String?
    ): TripCommonItem? {
        // Log.d("gettingTripItemCommon", "TripCommonItemRepository: Starting API call")

        return try {
            val response = api.getCommonTripItem(
                serviceKey = myKey,
                mobileOS = "ETC",
                mobileApp = "com.lion.wandertrip",
                type = "json",
                contentId = contentId,
                numOfRows = 10,
                pageNo = 1
            )

            if (response.isSuccessful) {
                // Log.d("gettingTripItemCommon", "API request was successful")
                // Log.d("gettingTripItemCommon", "Response Body: ${response.body()}")

                // 응답이 성공적이라면, 응답 데이터에서 필요한 정보를 추출하여 TripCommonItem으로 매핑
                response.body()?.let { apiResponse ->
                    val item = apiResponse.response.body.items.item.firstOrNull() // 첫 번째 항목을 가져옵니다.
                    // 매핑하여 반환
                    // Log.d("test","item: $item")
                    item?.let {
                        TripCommonItem(
                            contentId = it.contentId,
                            contentTypeId = it.contentTypeId,
                            title = it.title,
                            tel = it.tel,
                            homepage = it.homepage,
                            firstImage = it.firstImage,
                            siGunGuCode = it.siGunGuCode,
                            areaCode = it.areaCode,
                            addr1 = it.addr1,
                            addr2 = it.addr2,
                            mapLat = it.mapLat,
                            mapLng = it.mapLng,
                            overview = it.overview
                        )
                    }
                }
            } else {
                Log.d("gettingTripItemCommon", "API request failed: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("gettingTripItemCommon", "Error occurred while fetching trip item", e)
            null
        }
    }


    // 사용자의 관심 콘텐츠 리스트를 기반으로 여행 아이템 정보를 가져오는 함수
    suspend fun gettingTripItemCommonInteresting(
        contentIdList: MutableList<String>, // 관심 있는 콘텐츠 ID 리스트
    ): MutableList<UserInterestingModel> {
        return try {

            val interestingList = mutableListOf<UserInterestingModel>()

            contentIdList.forEach { contentId ->
                //Log.d("gettingTripItemCommonInteresting", "📌 API 요청 시작 | 콘텐츠 ID: $contentId")

                val response = api.getCommonTripItem(
                    serviceKey = myKey,
                    mobileOS = "ETC",
                    mobileApp = "com.lion.wandertrip",
                    type = "json",
                    contentId = contentId,
                    numOfRows = 10,
                    pageNo = 1,

                )

                if (response.isSuccessful) {

                    response.body()?.let { apiResponse ->
                        val item = apiResponse.response.body.items.item.firstOrNull()

                        if (item != null) {
                            //Log.d("gettingTripItemCommonInteresting", "🎯 API 응답 데이터 확인 | 제목: ${item.title ?: "제목 없음"}")

                            interestingList.add(
                                UserInterestingModel(
                                    contentID = item.contentId ?: "none",
                                    contentTypeID = item.contentTypeId ?: "none",
                                    contentTitle = item.title ?: "제목 없음",
                                    smallImagePath = item.firstImage ?: "이미지 없음",
                                    areacode = item.areaCode ?: "지역 코드 없음",
                                    sigungucode = item.siGunGuCode ?: "시군구 코드 없음",
                                    ratingScore = 0.0f,
                                    starRatingCount = 0,
                                    saveCount = 0,
                                    cat2 = item.cat2 ?: "none",
                                    cat3 = item.cat3 ?: "none",
                                    addr1 = item.addr1 ?: "none",
                                    addr2 = item.addr2 ?: "none"
                                )
                            )
                        } else {
                            Log.d("gettingTripItemCommonInteresting", "⚠️ API 응답은 성공했지만 데이터가 비어 있음 | 콘텐츠 ID: $contentId")
                        }
                    }
                } else {
                    Log.e(
                        "gettingTripItemCommonInteresting",
                        "❌ API 요청 실패 | HTTP 코드: ${response.code()} | 오류 메시지: ${response.message()}"
                    )
                }
            }

            interestingList

        } catch (e: Exception) {
            Log.e("gettingTripItemCommonInteresting", "🚨 API 요청 중 오류 발생", e)
            mutableListOf()
        }
    }

}