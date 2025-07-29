package com.lion.wandertrip.retrofit_for_practice

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TripAreaBaseItemInterface {
    @GET("areaBasedList2")
    suspend fun getAreaBaseTripItem(
        @Query("serviceKey") serviceKey: String,
        @Query("MobileOS") mobileOS: String,
        @Query("MobileApp") mobileApp: String,
        @Query("_type") type: String? = null,
        @Query("contentId") contentId: String? = null,
        @Query("contentTypeId") contentTypeId: String? = null,
        @Query("defaultYN") defaultYN: String? = null,
        @Query("firstImageYN") firstImageYN: String? = null,
        @Query("areacodeYN") areacodeYN: String? = null,
        @Query("catcodeYN") catcodeYN: String? = null,
        @Query("addrinfoYN") addrinfoYN: String? = null,
        @Query("mapinfoYN") mapinfoYN: String? = null,
        @Query("overviewYN") overviewYN: String? = null,
        @Query("numOfRows") numOfRows: Int? = null,
        @Query("pageNo") pageNo: Int? = null
    ): Response<TripApiResponse>
}