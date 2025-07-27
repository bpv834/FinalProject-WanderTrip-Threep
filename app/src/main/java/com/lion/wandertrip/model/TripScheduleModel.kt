package com.lion.wandertrip.model

import com.google.firebase.Timestamp
import com.lion.wandertrip.vo.TripScheduleVO
import com.lion.wandertrip.vo.ScheduleItemVO

class TripScheduleModel(
    var tripScheduleDocId: String = "",                              // 문서 ID
    var userID: String = "",                                         // 유저 ID
    var userNickName: String = "",                                   // 유저 닉네임
    var scheduleTitle: String = "",                                  // 일정 제목
    var scheduleStartDate: Timestamp = Timestamp.now(),              // 일정 시작 날짜
    var scheduleCity: String = "",                                   // 일정 도시
    var scheduleEndDate: Timestamp = Timestamp.now(),                // 일정 끝 날짜
    var scheduleInviteList: List<String> = emptyList(),              // 함께하는 유저 문서 ID 리스트
    var scheduleDateList: List<Timestamp> = emptyList(),             // 일정 날짜 목록
    var scheduleTimeStamp: Timestamp = Timestamp.now(),              // 데이터가 들어온 시간
    var scheduleState: Int = 1,                                      // 일정 상태 (1: 활성화, 2: 비활성화)
    var scheduleItems: List<ScheduleItem> = emptyList(),             // 서브 컬렉션 (일정 내 아이템 리스트)
    var lat: Double = 0.0,
    var lng: Double = 0.0,
) {
    fun toTripScheduleVO(): TripScheduleVO {
        val tripScheduleVO = TripScheduleVO()
        tripScheduleVO.tripScheduleDocId = tripScheduleDocId
        tripScheduleVO.userID = userID
        tripScheduleVO.userNickName = userNickName
        tripScheduleVO.scheduleTitle = scheduleTitle
        tripScheduleVO.scheduleStartDate = scheduleStartDate
        tripScheduleVO.scheduleCity = scheduleCity
        tripScheduleVO.scheduleEndDate = scheduleEndDate
        tripScheduleVO.scheduleInviteList = scheduleInviteList
        tripScheduleVO.scheduleDateList = scheduleDateList
        tripScheduleVO.scheduleTimeStamp = scheduleTimeStamp
        tripScheduleVO.scheduleState = scheduleState
        tripScheduleVO.scheduleItems = scheduleItems.map { it.toScheduleItemVO() }
        tripScheduleVO.lat = lat
        tripScheduleVO.lng = lng

        return tripScheduleVO
    }
}

data class ScheduleItem(
    // 서브컬렉션으로 할거면, var tripScheduleDocId: String = "",  이 필요함 인덱스 조절할때나, 삭제할때 필요함
    var itemDocId: String = "",                          // 문서 ID
    var itemDate: Timestamp = Timestamp.now(),           // 일정 날짜
    var itemIndex: Int = 0,                             // 순서값
    var itemTitle: String = "",                         // 아이템 이름
    var itemType: String = "",                          // 목적지 분류 (관광지, 음식점, 숙소 중)
    var itemLongitude: Double = 0.0,                    // 경도(X)
    var itemLatitude: Double = 0.0,                     // 위도(Y)
    var itemImagesURL: List<String> = emptyList(),       // 아이템 이미지 URL 리스트
    var itemReviewText: String = "",                     // 후기 내용
    var itemReviewImagesURL: List<String> = emptyList(), // 후기 이미지 리스트
    var itemContentId: String = "",                         // 콘텐츠 ID
) {
    fun toScheduleItemVO(): ScheduleItemVO {
        val scheduleItemVO = ScheduleItemVO()
        scheduleItemVO.itemDocId = itemDocId
        scheduleItemVO.itemDate = itemDate
        scheduleItemVO.itemIndex = itemIndex
        scheduleItemVO.itemTitle = itemTitle
        scheduleItemVO.itemType = itemType
        scheduleItemVO.itemLongitude = itemLongitude
        scheduleItemVO.itemLatitude = itemLatitude
        scheduleItemVO.itemImagesURL = itemImagesURL
        scheduleItemVO.itemReviewText = itemReviewText
        scheduleItemVO.itemReviewImagesURL = itemReviewImagesURL
        scheduleItemVO.itemContentId = itemContentId
        return scheduleItemVO
    }

    fun copy(
        itemReviewImagesURL: List<String> = emptyList(),
        itemReviewText: String = ""
    ): ScheduleItemVO {
        return ScheduleItemVO(
            itemReviewImagesURL = itemReviewImagesURL,
            itemReviewText = itemReviewText
        )
    }
}
