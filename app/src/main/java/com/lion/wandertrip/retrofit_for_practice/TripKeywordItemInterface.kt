package com.lion.wandertrip.retrofit_for_practice

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TripKeywordItemInterface {
    @GET("searchKeyword2")
    suspend fun getKeywordTripItem(
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("pageNo") pageNo: Int = 1,
        @Query("MobileOS") mobileOS: String = "ETC",
        @Query("MobileApp") mobileApp: String = "com.lion.wandertrip",
        @Query("_type") type: String = "json",
        @Query("arrange") arrange: String = "O",
        @Query("keyword") keyword: String,
        @Query("serviceKey") serviceKey: String
    ): Response<TripApiResponse>
}