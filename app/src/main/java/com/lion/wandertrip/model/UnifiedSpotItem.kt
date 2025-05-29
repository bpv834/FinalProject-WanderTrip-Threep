package com.lion.wandertrip.model

data class UnifiedSpotItem(
    // 공공 데이터 (필수)
    val publicData: TripLocationBasedItem,
    // 개인 데이터 (있을 수도 있고 없을 수도 있음)
    // Map에서 찾지 못하면 null이 될 수 있으므로 Nullable로 선언
    val privateData: ContentsModel?
) {
}