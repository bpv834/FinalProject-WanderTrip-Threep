package com.lion.wandertrip.model

data class PopularCityModel(
    val rank: Int,
    val name: String,
    val imageUrl: String,
    val lat : Double,
    val lng : Double,
    val radius : Int,
) {
}