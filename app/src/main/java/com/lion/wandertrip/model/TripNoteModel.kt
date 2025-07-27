package com.lion.wandertrip.model

import com.google.firebase.Timestamp
import com.lion.wandertrip.vo.TripNoteVO

data class TripNoteModel(
    var tripNoteDocumentId: String = "",          // 여행기 문서 ID
    var userNickname: String = "",                // 여행기 작성자 닉네임
    var tripNoteTitle: String = "",               // 여행기 제목
    var tripNoteContent: String = "",             // 여행기 내용
    var tripNoteImage: List<String> = emptyList(),// 여행기 대표 사진(썸네일) 리스트
    var tripScheduleDocumentId: String = "",      // 여행 일정 문서 ID
    var tripNoteScrapCount: Int = 0,              // 담아간 개수 (스크랩 횟수)
    var tripNoteTimeStamp: Timestamp = Timestamp.now(), // 데이터 입력 시간 (Firestore Timestamp)
    var tripNoteState: Int = 1,                    // 여행기 상태 (1: 활성화, 2: 비활성화)
    var location :String = "",
    var lat:Double = 0.0,
    var lng:Double = 0.0

) {
    fun toTripNoteVO(): TripNoteVO {
        val tripNoteVO = TripNoteVO()
        tripNoteVO.tripNoteDocumentId = tripNoteDocumentId
        tripNoteVO.userNickname = userNickname
        tripNoteVO.tripNoteTitle = tripNoteTitle
        tripNoteVO.tripNoteContent = tripNoteContent
        tripNoteVO.tripNoteImage = tripNoteImage
        tripNoteVO.tripScheduleDocumentId = tripScheduleDocumentId
        tripNoteVO.tripNoteScrapCount = tripNoteScrapCount
        tripNoteVO.tripNoteTimeStamp = tripNoteTimeStamp
        tripNoteVO.tripNoteState = tripNoteState
        tripNoteVO.location=location
        tripNoteVO.lat=lat
        tripNoteVO.lng=lng
        return tripNoteVO
    }
}
