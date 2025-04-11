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
        @Query("arrange") arrange: String = "Q",
        @Query("keyword") keyword: String,
        @Query("serviceKey") serviceKey: String
    ): Response<TripCommonItemsApiResponse>
}