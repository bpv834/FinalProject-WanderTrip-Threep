package com.lion.wandertrip.retrofit_for_practice

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TripCommonItemInterface {
    @GET("detailCommon2")
    suspend fun getCommonTripItem(
        @Query("serviceKey") serviceKey: String,
        @Query("MobileOS") mobileOS: String,
        @Query("MobileApp") mobileApp: String,
        @Query("_type") type: String,
        @Query("contentId") contentId: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int
    ): Response<TripApiResponse>
}