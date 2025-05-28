package com.lion.wandertrip.retrofit_for_practice

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TripLocationBasedInterface {
    @GET("/locationBasedList2")
    fun getLocationBasedList(
        @Query("numOfRows") numOfRows: Int?,
        @Query("pageNo") pageNo: Int?,
        @Query("MobileOS") mobileOS: String,
        @Query("MobileApp") mobileApp: String,
        @Query("_type") type: String?,
        @Query("arrange") arrange: String?,
        @Query("mapY") mapY: String,
        @Query("mapX") mapX: String,
        @Query("radius") radius: String,
        @Query("contentTypeId") contentTypeId: String?,
        @Query("serviceKey") serviceKey: String,
    ): Response<TripApiResponse>
}