package com.lion.wandertrip.util

data class PopularCity(
    val rank: Int,
    val name: String,
    val imageUrl: String,
    val lat : Double,
    val lng : Double,
    val radius : Int,
) {
}