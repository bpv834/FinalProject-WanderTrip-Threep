package com.lion.wandertrip.service

import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.repository.TripAreaBaseItem2Repository
import com.lion.wandertrip.vo.TripItemVO

class TripAreaBaseItem2Service(val repository : TripAreaBaseItem2Repository) {
    suspend fun gettingAllItemWithAreaCode(areaCode:String,sigunguCode:String?):List<TripItemModel>?{
        return repository.gettingAllItemWithAreaCode(areaCode,sigunguCode)
    }
}