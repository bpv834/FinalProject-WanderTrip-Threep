package com.lion.wandertrip.service

import android.net.Uri
import android.util.Log
import com.lion.wandertrip.model.ReviewModel
import com.lion.wandertrip.repository.ContentsReviewRepository

class ContentsReviewService(val contentsReviewRepository: ContentsReviewRepository) {

    // 사용자의 리뷰 문서 가져오기
    suspend fun getContentsUserReviewByNickName(contentsWriterNickName: String): MutableList<ReviewModel> {
        val voList = contentsReviewRepository.getContentsUserReviewByNickName(contentsWriterNickName)
        val resultList = mutableListOf<ReviewModel>()
        voList.forEach {
            resultList.add(it.toReviewItemModel())
        }
        return resultList
    }


    // 해당 리뷰 가져오기
    suspend fun getContentsReviewByDocId( contentsReviewDocId: String): ReviewModel {
        var reviewModel = ReviewModel()

        try {
            val reviewVO = contentsReviewRepository.getContentsReviewByDocId(contentsReviewDocId)

            // 변환 성공 시
            reviewModel = reviewVO.toReviewItemModel()

        } catch (e: Exception) {
            // 예외 발생 시
            Log.e("test100", "리뷰 문서 가져오기 실패: $contentsReviewDocId", e)
        }

        return reviewModel
    }

    // 해당 컨텐츠에 있는 모든 리뷰 문서 가져오기
    suspend fun getAllReviewsWithContents(contentsDocId: String): MutableList<ReviewModel> {
        // 리뷰를 저장할 리스트 초기화
        val modelList = mutableListOf<ReviewModel>()

        try {
            // 컨텐츠에 관련된 모든 리뷰를 가져옴
            val voList = contentsReviewRepository.getAllReviewsWithContents(contentsDocId)

            // 가져온 ReviewVO 리스트를 ReviewModel로 변환하여 리스트에 추가
            voList.forEach {
                modelList.add(it.toReviewItemModel())
            }

            // 성공적으로 리뷰들을 가져왔다면 로그 출력
        } catch (e: Exception) {
            // 예외 발생 시 로그에 오류 메시지를 출력하고 빈 리스트를 반환
            Log.e("test100", "모든 리뷰 문서 가져오기 실패: $contentsDocId", e)
        }

        // 변환된 리뷰 모델 리스트 반환
        return modelList
    }

    // 리뷰 등록
    suspend fun addContentsReview(contentsReviewModel: ReviewModel): String {
         try {
            // 리뷰 추가를 위한 repository 메서드 호출
            val result = contentsReviewRepository.addContentsReview(contentsReviewModel.toReviewItemVO())

            // 리뷰 추가 성공 시 로그 출력

            return result // 성공한 경우 true 반환
        } catch (e: Exception) {
             // 예외 발생 시 로그에 오류 메시지 출력
             Log.e("ContentsReviewService", "리뷰 등록 실패: ", e)
            return ""
         }
    }

    // 리뷰 수정하기
    suspend fun modifyContentsReview(reviewModel: ReviewModel): Boolean {
        return try {
            // 리뷰 수정 요청을 위한 repository 메서드 호출 전 로그 추가
            //Log.d("ContentsReviewService", "리뷰 수정 시작: contentsDocID = $contentsDocID, reviewModel = $reviewModel")

            // 리뷰 수정 요청
            val result = contentsReviewRepository.modifyContentsReview(reviewModel.toReviewItemVO())

            // 성공 로그
            if (result) {
               // Log.d("ContentsReviewService", "리뷰 수정 성공: contentsDocID = $contentsDocID, reviewDocId = ${reviewModel.reviewDocId}")
            } else {
                //Log.d("ContentsReviewService", "리뷰 수정 실패 (기타): contentsDocID = $contentsDocID")
            }

            result // 성공한 경우 true 반환
        } catch (e: Exception) {
            // 예외 발생 시 에러 로그에 구체적인 메시지 출력
            // 예외를 출력하여 문제 파악에 도움을 줍니다.
            Log.e("test100", "에러 메시지: ${e.message}")
            Log.e("test100", "스택 트레이스: ${Log.getStackTraceString(e)}")

            // 실패 시 false 반환
            false
        }
    }


    // 이미지 데이터를 서버로 업로드 하는 메서드
    suspend fun uploadReviewImageList(sourceFilePath: List<String>, serverFilePath: MutableList<String>, contentsId: String) :List<String>  {
       return contentsReviewRepository.uploadReviewImageList(sourceFilePath,serverFilePath,contentsId)
    }

    // 이미지 Uri 리스트를 가져오는 메서드
    suspend fun gettingReviewImage(imageFileNameList: List<String>,contentsId: String): List<Uri> {
        return contentsReviewRepository.gettingReviewImageList(imageFileNameList,contentsId)
    }

    // 리뷰 삭제 메서드
    suspend fun deleteContentsReview( contentsReviewDocId: String) {
        contentsReviewRepository.deleteContentsReview(contentsReviewDocId)
    }

    // 해당 컨텐츠에 리뷰 문서 개수 리턴받기
    suspend fun getAllReviewsCountWithContents(contentID: String): Int {
        Log.d("test100","getAllReviewsCountWithContents")
        return try {
            //Log.d("test100", "📌 리뷰 개수 조회 시작: contentsDocId = $contentID")

            // 컨텐츠에 관련된 모든 리뷰를 가져옴
            val voList = contentsReviewRepository.getAllReviewsWithContents(contentID)

            // 가져온 리스트가 null이 아닌지 확인
           // Log.d("test100", "📌 가져온 리뷰 리스트: $voList")

            // 리스트 개수 확인 후 리턴
            val count = voList.size
            //Log.d("test100", "✅ 리뷰 개수: $count")

            count
        } catch (e: Exception) {
            // 예외 발생 시 오류 로그 출력
            Log.e("test100", "❌ 모든 리뷰 문서 가져오기 실패: $contentID", e)
            0 // 예외 발생 시 0 반환
        }
    }

    //닉네임 바꿀 때 사용하기
    // 닉변 전 게시물의 닉네임을 변경한 닉네임으로 update
    suspend fun changeReviewNickName(oldNickName: String, newNickName: String) {
        contentsReviewRepository.changeReviewNickName(oldNickName,newNickName)
    }

    // 유저의 리뷰 개수 카운팅
    suspend fun getCountUserReview(userNickName : String): Int{
        return getContentsUserReviewByNickName(userNickName).size
    }


}