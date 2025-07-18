package com.lion.wandertrip.model

import com.google.firebase.Timestamp
import com.lion.wandertrip.vo.ReviewVO

class ReviewModel(
    var reviewDocId: String = "",    // 리뷰 문서 ID
    var reviewWriterNickname: String = "", // 작성자 닉네임
    var reviewContent: String = "",  // 리뷰 내용
    var reviewTimeStamp: Timestamp = Timestamp.now(), // 리뷰 작성 시간
    var reviewRatingScore: Float = 0.0f,  // 리뷰 별점 // 별점은 플롯으로
    var reviewImageList: List<String> = emptyList(), // 리뷰 이미지 리스트
    var reviewTitle :String = "",// 리뷰 제목
    var reviewState : Int = 1, // 리뷰 상태 1: 노출 , 2: 삭제됨
    var reviewWriterProfileImgURl : String ="",
    var contentId : String = ""
) {
    fun toReviewItemVO(): ReviewVO {
        val reviewItemVO = ReviewVO()
        reviewItemVO.reviewDocId = reviewDocId
        reviewItemVO.reviewWriterNickname = reviewWriterNickname
        reviewItemVO.reviewContent = reviewContent
        reviewItemVO.reviewTimeStamp = reviewTimeStamp
        reviewItemVO.reviewRatingScore = reviewRatingScore
        reviewItemVO.reviewImageList = reviewImageList
        reviewItemVO.reviewTitle = reviewTitle
        reviewItemVO.reviewState = reviewState
        reviewItemVO.reviewWriterProfileImgURl = reviewWriterProfileImgURl
        reviewItemVO.contentId = contentId
        return reviewItemVO
    }
}
