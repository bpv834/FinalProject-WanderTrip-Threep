package com.lion.wandertrip.vo

import com.google.firebase.Timestamp
import com.lion.wandertrip.model.TripScheduleModel
import com.lion.wandertrip.model.ScheduleItem

class TripScheduleVO(
    var tripScheduleDocId: String = "",
    var userID: String = "",
    var userNickName: String = "",
    var scheduleTitle: String = "",
    var scheduleStartDate: Timestamp = Timestamp.now(),
    var scheduleCity: String = "",
    var scheduleEndDate: Timestamp = Timestamp.now(),
    var scheduleInviteList: List<String> = emptyList(),
    var scheduleDateList: List<Timestamp> = emptyList(),
    var scheduleTimeStamp: Timestamp = Timestamp.now(),
    var scheduleState: Int = 1,
    var scheduleItems: List<ScheduleItemVO> = emptyList(),
    var lat : String? =null,
    var lng : String? =null,
) {

    fun toTripScheduleModel(): TripScheduleModel {
        val tripScheduleModel = TripScheduleModel()
        tripScheduleModel.tripScheduleDocId = tripScheduleDocId
        tripScheduleModel.userID = userID
        tripScheduleModel.userNickName = userNickName
        tripScheduleModel.scheduleTitle = scheduleTitle
        tripScheduleModel.scheduleStartDate = scheduleStartDate
        tripScheduleModel.scheduleCity = scheduleCity
        tripScheduleModel.scheduleEndDate = scheduleEndDate
        tripScheduleModel.scheduleInviteList = scheduleInviteList
        tripScheduleModel.scheduleDateList = scheduleDateList
        tripScheduleModel.scheduleTimeStamp = scheduleTimeStamp
        tripScheduleModel.scheduleState = scheduleState
        tripScheduleModel.scheduleItems = scheduleItems.map { it.toScheduleItemModel() }
        tripScheduleModel.lat = lat
        tripScheduleModel.lng = lng
        return tripScheduleModel
    }
}

class ScheduleItemVO(
    var itemDocId: String = "",
    var itemDate: Timestamp = Timestamp.now(),
    var itemIndex: Int = 0,
    var itemTitle: String = "",
    var itemType: String = "",
    var itemLongitude: Double = 0.0,
    var itemLatitude: Double = 0.0,
    var itemImagesURL: List<String> = emptyList(),
    var itemReviewText: String = "",
    var itemReviewImagesURL: List<String> = emptyList(),
    var itemContentId: String = "",
) {

    fun toScheduleItemModel(): ScheduleItem {
        val scheduleItemModel = ScheduleItem()
        scheduleItemModel.itemDocId = itemDocId
        scheduleItemModel.itemDate = itemDate
        scheduleItemModel.itemIndex = itemIndex
        scheduleItemModel.itemTitle = itemTitle
        scheduleItemModel.itemType = itemType
        scheduleItemModel.itemLongitude = itemLongitude
        scheduleItemModel.itemLatitude = itemLatitude
        scheduleItemModel.itemImagesURL = itemImagesURL
        scheduleItemModel.itemReviewText = itemReviewText
        scheduleItemModel.itemReviewImagesURL = itemReviewImagesURL
        scheduleItemModel.itemContentId = itemContentId
        return scheduleItemModel
    }
}
