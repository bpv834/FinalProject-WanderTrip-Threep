package com.lion.wandertrip.model

data class TripLocationBasedItem(
    var contentId: String?="",
    var contentTypeId: String?="",
    var title: String?="",
    var firstImage: String?="",
    var areaCode: String?="",
    var siGunGuCode: String?="",
    var addr1: String?="",
    var addr2: String?="",
    var mapLat: String?="",
    var mapLng: String?="",
) {
}