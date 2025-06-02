package com.lion.wandertrip.repository

import android.util.Log
import com.lion.wandertrip.model.TripLocationBasedItem
import com.lion.wandertrip.retrofit_for_practice.TripLocationBasedInterface
import retrofit2.HttpException // HTTP 오류 처리를 위함

class TripLocationBasedItemRepository(private val api: TripLocationBasedInterface) {
    val myKey =
        "qe8EY3d1ixNdu4/lC0aXJXbTH/VndGcoj5DABUigtfSCLIIP48IHXbwMEkG5gkGvVW/wKl1XuuFyqYwwWQZJDg=="

    suspend fun gettingTripLocationBased(
        lat: String, lng: String, contentTypeId: String, page: Int = 1, radius: String,numOfRows : Int = 10
    ): Pair<List<TripLocationBasedItem>, Int> {
        // 로그 찍기
        //Log.d("gettingTripLocationBased", "요청 파라미터 - lat: $lat, lng: $lng, contentTypeId: $contentTypeId, page: $page, radius: $radius")

        try {
            val response = api.getLocationBasedList(
                serviceKey = myKey,
                mobileOS = "ETC",
                mobileApp = "com.lion.wandertrip",
                type = "json",
                contentTypeId = contentTypeId,
                numOfRows = numOfRows,
                pageNo = page,
                mapX = lng,
                mapY = lat,
                radius = (radius.toInt()*1000).toString(),
                arrange = "O", // 대표이미지가 있는 정렬 (제목순)
            )


            if (response.isSuccessful) {
                // 응답 본문이 null이 아니고, 항목 리스트가 비어있지 않은지 확인
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.response.body.items.item.isNotEmpty()) {
                    val items = apiResponse.response.body.items.item
                    Log.d("gettingTripLocationBased", "성공적으로 ${items.size}개의 항목을 가져왔습니다.")
                    // Log.d("TripRepo", "첫 번째 항목 제목: ${items.firstOrNull()?.title}")
                    val totalCount = apiResponse.response.body.totalCount
                    val list = mutableListOf<TripLocationBasedItem>()

                    items.forEach {
                        val model = TripLocationBasedItem()
                        model.mapLat =it.mapLat
                        model.mapLng = it.mapLng
                        model.addr1 = it.addr1
                        model.addr2 = it.addr2
                        model.areaCode = it.areaCode
                        model.siGunGuCode = it.siGunGuCode
                        model.contentId = it.contentId
                        model.contentTypeId = it.contentTypeId
                        model.title = it.title
                        model.firstImage = it.firstImage

                        list.add(model)
                    }
                    val result = mutableListOf<Any>()
                    result.add(list)
                    result.add(totalCount)

                    return Pair(list, totalCount)
                } else {
                    Log.d("gettingTripLocationBased", "API 응답 본문이 null이거나 항목 리스트가 비어 있습니다.")
                    return Pair(emptyList(),0) // 항목이 없으면 빈 리스트 반환
                }
            } else {
                // API 요청 실패 시
                Log.e("gettingTripLocationBased", "API 요청 실패: ${response.code()} - ${response.message()}")
                Log.e("gettingTripLocationBased", "오류 본문: ${response.errorBody()?.string()}")
                return Pair(emptyList(),0) // 항목이 없으면 빈 리스트 반환
            }
        } catch (e: HttpException) {
            // HTTP 관련 오류 (예: 404, 500) 처리
            Log.e("gettingTripLocationBased", "HTTP 오류 발생: ${e.code()} - ${e.message()}", e)
            return Pair(emptyList(),0)
        } catch (e: Exception) {
            // 네트워크 문제, JSON 파싱 오류 등 기타 예외 처리
            Log.e("gettingTripLocationBased", "여행 항목을 가져오는 중 오류 발생: ${e.message}", e)
            return Pair(emptyList(),0)
        }
    }
}