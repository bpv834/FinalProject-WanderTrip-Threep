package com.lion.wandertrip

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.lion.wandertrip.model.TripItemModel
import com.lion.wandertrip.service.TripKeywordItemService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val tripKeywordItemService: TripKeywordItemService
) : ViewModel() {
    val tripApplication = context as TripApplication

    // suspend 함수로 변경: csv 파일에서 인기 관광지명을 리스트를 얻어 리턴받는다
    suspend fun getHotSpotList(): MutableList<String> = withContext(Dispatchers.IO) {
        val result = mutableListOf<String>()

        try {
            // assets 디렉토리에서 파일 열기
            tripApplication.assets.open("인기관광지.csv").bufferedReader().useLines { lines ->
                lines.drop(1) // 헤더 라인 건너뛰기
                    .forEach { line ->
                        val tokens = line.split(",")
                        if (tokens.size >= 3) {
                            val spotName = tokens[2].trim()
                            result.add(spotName)
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("SplashViewModel", "CSV 파일 읽기 오류: ${e.localizedMessage}")
        }

        result
    }

    // 관광지명으로 tripItemModel 를 찾는 메서드 -> 찾아 모듈에 리스트 저장
    suspend fun getTripModelList(list: MutableList<String>) = withContext(Dispatchers.IO) {
        Log.d("SplashViewModel","getTripModelList")
        val tripList = mutableListOf<TripItemModel>()
        list.forEach {
            val item = tripKeywordItemService.gettingTripItemByKeyword(it)
            if(item!=null)
            tripList.add(item ?: TripItemModel())
        }
        tripApplication.popularTripList.clear()
        // 모듈에 저장한다
        tripApplication.popularTripList.addAll(tripList)
    }

    // 두 함수를 순차적으로 동작시킨다.
    suspend fun fetchTripItemModel() {
        val list = getHotSpotList()
        getTripModelList(list)
    }
}