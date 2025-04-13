package com.lion.wandertrip.model

import com.lion.wandertrip.vo.TripItemVO

class TripItemModel(
    var addr1: String = "",          // 주소
    var addr2: String = "",          // 상세주소
    var areaCode: String = "",       // 지역코드
    var sigunguCode : String = "",   // 시군구 코드
    var contentId: String = "",      // 항목 ID
    var contentTypeId: String = "",  // 12: 관광지, 32: 숙박, 39: 음식점
    var firstImage: String = "",     // 이미지 URi
    var mapLat: Double = 0.0,        // 위도(Y)
    var mapLong: Double = 0.0,       // 경도(X)
    var tel: String = "",            // 전화번호
    var title: String = "",          // 제목
    var cat1 : String = "",          // 대분류
    var cat2 : String = "",          // 중분류
    var cat3 : String = "",          // 소분류
    // 큰 이미지도 가져와야 함
) {
    fun toTripItemVO(): TripItemVO {
        val tripItemVO = TripItemVO()
        tripItemVO.addr1 = addr1
        tripItemVO.addr2 = addr2
        tripItemVO.areaCode = areaCode
        tripItemVO.contentId = contentId
        tripItemVO.contentTypeId = contentTypeId
        tripItemVO.firstImage = firstImage
        tripItemVO.mapLat = mapLat
        tripItemVO.mapLong = mapLong
        tripItemVO.tel = tel
        tripItemVO.title = title
        tripItemVO.cat1 = cat1
        tripItemVO.cat2 = cat2
        tripItemVO.cat3 = cat3
        return tripItemVO
    }
}
