package com.lion.wandertrip.retrofit_for_practice

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TripCommonItemInterface {
    @GET("detailCommon1")
    suspend fun getCommonTripItem(
        @Query("serviceKey") serviceKey: String,
        @Query("MobileOS") mobileOS: String,
        @Query("MobileApp") mobileApp: String,
        @Query("_type") type: String,
        @Query("contentId") contentId: String,
        @Query("contentTypeId") contentTypeId: String,
        @Query("defaultYN") defaultYN: String,
        @Query("firstImageYN") firstImageYN: String,
        @Query("areacodeYN") areacodeYN: String,
        @Query("catcodeYN") catcodeYN: String,
        @Query("addrinfoYN") addrinfoYN: String,
        @Query("mapinfoYN") mapinfoYN: String,
        @Query("overviewYN") overviewYN: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int
    ): Response<TripApiResponse>
}