package com.lion.wandertrip.retrofit_for_practice

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TripKeywordItemInterface {
    @GET("searchKeyword1")
    suspend fun getKeywordTripItem(
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("pageNo") pageNo: Int = 1,
        @Query("MobileOS") mobileOS: String = "ETC",
        @Query("MobileApp") mobileApp: String = "AppTest",
        @Query("_type") type: String = "json",
        @Query("listYN") listYN: String = "Y",
        @Query("arrange") arrange: String = "O",
        @Query("keyword") keyword: String,
        @Query("serviceKey") serviceKey: String
    ): Response<TripApiResponse>
}

interface TourApi {
    @GET("searchKeyword2")
    suspend fun getKeywordTripItemV2(
        @Query("numOfRows") numOfRows: Int = 1000,
        @Query("pageNo") pageNo: Int = 1,
        @Query("MobileOS") mobileOS: String = "ETC",
        @Query("MobileApp") mobileApp: String = "WanderTrip",
        @Query("_type") type: String = "json",
        @Query("arrange") arrange: String = "O",
        @Query("listYN") listYN: String = "Y", // ← 문서엔 없어도 안전하게 넣기
        @Query("keyword") keyword: String,
        @Query("serviceKey") serviceKey: String
    ): Response<TripApiResponse>
}