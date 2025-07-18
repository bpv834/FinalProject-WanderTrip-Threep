package com.lion.wandertrip.vo

import com.google.firebase.Timestamp
import com.lion.wandertrip.model.ReviewModel

class ReviewVO {
    var reviewDocId: String = ""
    var reviewWriterNickname: String = ""
    var reviewContent: String = ""
    var reviewTimeStamp: Timestamp = Timestamp.now()
    var reviewRatingScore: Float = 0.0f // 별점은 플롯으로
    var reviewImageList: List<String> = emptyList()
    var reviewTitle :String = "" // 리뷰 제목
    var reviewState : Int = 1 // 리뷰 상태 1: 노출 , 2: 삭제됨
    var reviewWriterProfileImgURl : String =""
    var contentId : String = ""



    fun toReviewItemModel(): ReviewModel {
        val reviewItemModel = ReviewModel()
        reviewItemModel.reviewDocId = reviewDocId
        reviewItemModel.reviewWriterNickname = reviewWriterNickname
        reviewItemModel.reviewContent = reviewContent
        reviewItemModel.reviewTimeStamp = reviewTimeStamp
        reviewItemModel.reviewRatingScore = reviewRatingScore
        reviewItemModel.reviewImageList = reviewImageList
        reviewItemModel.reviewTitle = reviewTitle
        reviewItemModel.reviewState = reviewState
        reviewItemModel.reviewWriterProfileImgURl = reviewWriterProfileImgURl
        reviewItemModel.contentId = contentId
        return reviewItemModel
    }
}
