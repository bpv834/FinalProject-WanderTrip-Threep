package com.lion.wandertrip.repository

import android.util.Log
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.retrofit_for_practice.TripAreaBaseItem2Interface

class TripAreaBaseItem2Repository(private val api: TripAreaBaseItem2Interface) {
    val myKey =
        "qe8EY3d1ixNdu4/lC0aXJXbTH/VndGcoj5DABUigtfSCLIIP48IHXbwMEkG5gkGvVW/wKl1XuuFyqYwwWQZJDg=="

    // 특정 지역(areaCode)과 시군구(sigunguCode)에 해당하는 관광 데이터를 가져와 TripItemModel 리스트로 반환하는 suspend 함수
    suspend fun gettingAllItemWithAreaCode(areaCode: String, sigunguCode: String?): List<TripItemModel>? {
        return try {
            // Retrofit API 호출 - areaBasedList2 엔드포인트를 통해 데이터 요청
            val response = api.getAreaBasedList(
                serviceKey = myKey,                      // 공공데이터 포털에서 발급받은 인증키
                mobileOS = "ETC",                        // 플랫폼 정보 (ETC: 기타)
                mobileApp = "com.lion.wandertrip",       // 앱 이름 (임의로 지정 가능)
                type = "json",                           // 응답 형식 지정 (json)
                numOfRows = 10000,                       // 최대 10,000개의 항목 요청 (페이지당 결과 수)
                pageNo = 1,                              // 1페이지 요청
                areaCode = areaCode,                     // 지역코드 (필수)
                sigunguCode = sigunguCode,                // 시군구코드 (선택)
                arrange = "O"
            )

            // 응답이 성공적인 경우
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    // 응답에서 실제 데이터 항목(item) 리스트 추출
                    val items = apiResponse.response.body.items.item

                    // item 리스트를 TripItemModel 리스트로 변환
                    items.map {
                        TripItemModel(
                            contentId = it.contentId ?: "",                 // 콘텐츠 ID
                            contentTypeId = it.contentTypeId ?: "",         // 콘텐츠 타입 ID (12: 관광지 등)
                            title = it.title ?: "",                         // 제목 (장소 이름)
                            tel = it.tel ?: "",                             // 전화번호
                            firstImage = it.firstImage ?: "",               // 대표 이미지 URL
                            areaCode = it.areaCode ?: "",                   // 지역 코드
                            sigunguCode = it.siGunGuCode ?: "",             // 시군구 코드
                            addr1 = it.addr1 ?: "",                         // 기본 주소
                            addr2 = it.addr2 ?: "",                         // 상세 주소
                            mapLat = it.mapLat?.toDoubleOrNull() ?: 0.0,    // 위도 (문자열 → Double 변환, null이면 0.0)
                            mapLong = it.mapLng?.toDoubleOrNull() ?: 0.0,   // 경도 (mapLng → mapLong으로 이름 매칭)
                            cat2 = it.cat2 ?: "",                           // 카테고리 중분류
                            cat3 = it.cat3 ?: ""                            // 카테고리 소분류
                        )
                    }
                }
            } else {
                // HTTP 응답 실패 시 로그 출력 후 null 반환
                Log.d("API_ERROR", "API request failed: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            // 네트워크 오류나 예외 발생 시 로그 출력 후 null 반환
            Log.e("API_ERROR", "Error occurred while fetching tour items", e)
            null
        }
    }
}