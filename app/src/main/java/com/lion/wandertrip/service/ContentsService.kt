package com.lion.wandertrip.service

import com.lion.wandertrip.model.ContentsModel
import com.lion.wandertrip.repository.ContentsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContentsService(val contentsRepository: ContentsRepository) {
    // 컨텐츠 가져오는 서비스
    suspend fun getContentByDocId(contentDocId: String): ContentsModel {
        val contentsVO = contentsRepository.getContentByDocId(contentDocId) // 데이터를 가져옴
        return contentsVO.toDetailReviewModel() // 변환 후 반환
    }

    // 컨텐츠 가져오기 contentsID로
    suspend fun getContentByContentsId(contentsId: String): ContentsModel {
        return contentsRepository.getContentByContentsId(contentsId).toDetailReviewModel()
    }

    // 컨텐츠 넣기
    suspend fun addContents(contentModel: ContentsModel): String {
        return contentsRepository.addContents(contentModel.toDetailReviewVO())

    }

    // 컨텐츠 수정하기
    suspend fun modifyContents(contentModel: ContentsModel): Boolean {
        return contentsRepository.modifyContents(contentModel.toDetailReviewVO())
    }

    // 컨텐츠가 존재하면 true 리턴 contenstDocID 를 매개변수로 받을것
    suspend fun isContentExists(contentsDocId: String): String {
        return contentsRepository.isContentExists(contentsDocId) ?: ""
    }

    // 특정 컨텐츠의 리뷰 평점 평균을 계산하여 ContentsData 문서에 저장하고 리뷰 개수를 반환
    suspend fun updateContentRatingAndRatingCount(contentsDocId: String): Int {
        return contentsRepository.updateContentRatingAndRatingCount(contentsDocId)
    }

    // 컨텐츠 flow로 받기
    fun getContentsModelFlowByContentId(contentId: String): Flow<ContentsModel?> {
        // Repository에서 ContentsVO Flow를 받아와 ContentsModel Flow로 변환
        return contentsRepository.getContentsFlowByContentId(contentId)
            .map { contentsVO ->
                // contentsVO가 null이 아니면 toModel() 함수를 사용하여 ContentsModel로 변환
                contentsVO?.toDetailReviewModel()
            }
    }

    // 컨텐츠 리스트 flow로 받기
    fun getAllContentsModelsFlow(): Flow<List<ContentsModel>> {
        // Repository에서 모든 ContentsVO 리스트 Flow를 받아와 ContentsModel 리스트 Flow로 변환
        return contentsRepository.getAllContentsFlow()
            .map { voList ->
                // List<ContentsVO>의 각 요소를 ContentsModel로 변환하여 새로운 List<ContentsModel> 생성
                voList.map { it.toDetailReviewModel() }
            }
    }

    // 여기에 ContentsService의 비즈니스 로직을 추가할 수 있습니다.
    // 예: 별점 업데이트, 리뷰 추가 등 (이러한 작업은 Flow가 아닌 suspend 함수로 구현)
    // suspend fun updateRating(contentId: String, rating: Float) {
    //     // 내부적으로 ContentsRepository의 update 함수를 호출할 수 있습니다.
    //     // contentsRepository.updateRatingInFirestore(contentId, rating)
}

