package com.lion.wandertrip.vo

import com.google.firebase.Timestamp
import com.lion.wandertrip.model.TripNoteReplyModel

class TripNoteReplyVO {
    var tripNoteReplyDocId: String = ""
    var userNickname: String = ""
    var replyText: String = ""
    var replyState: Int = 1
    var replyTimeStamp: Timestamp = Timestamp.now()
    var tripNoteDocumentId: String = "" // 여행기 문서 ID

    fun toReplyItemModel(): TripNoteReplyModel {
        val replyItemModel = TripNoteReplyModel()
        replyItemModel.tripNoteReplyDocId = tripNoteReplyDocId
        replyItemModel.userNickname = userNickname
        replyItemModel.replyText = replyText
        replyItemModel.replyState = replyState
        replyItemModel.replyTimeStamp = replyTimeStamp
        replyItemModel.tripNoteDocumentId = tripNoteDocumentId
        return replyItemModel
    }
}
