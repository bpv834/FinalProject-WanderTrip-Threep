package com.lion.wandertrip.retrofit_for_practice

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TripAreaBaseItem2Interface {
    @GET("areaBasedList2")
    suspend fun getAreaBasedList(
        @Query("serviceKey") serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("pageNo") pageNo: Int = 1,
        @Query("MobileOS") mobileOS: String = "ETC",
        @Query("MobileApp") mobileApp: String = "com.lion.wandertrip",
        @Query("_type") type: String = "json",
        @Query("areaCode") areaCode: String? = null,
        @Query("sigunguCode") sigunguCode: String? = null,
        @Query("arrange") arrange: String? = null,
        @Query("contentTypeId") contentTypeId: String? = null,

    ): Response<TripApiResponse>
}