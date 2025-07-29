package com.lion.wandertrip.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.edit
import com.lion.wandertrip.model.UserModel
import com.lion.wandertrip.repository.UserRepository
import com.lion.wandertrip.util.LoginResult
import com.lion.wandertrip.util.UserState
import com.lion.wandertrip.vo.UserVO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class UserService (val userRepository: UserRepository) {

    // 가입하려는 아이디가 존재하는지 확인하는 메서드
    suspend fun checkJoinUserId(userId: String): Boolean {
        // 아이디를 통해 사용자 정보를 가져온다.
        val userVo: UserVO? = userRepository.selectUserDataByUserId(userId)

        // 가져온 데이터가 없다면 (즉, 아이디가 존재하지 않으면)
        if (userVo == null) {
            return true
        }
        // 가져온 데이터가 있다면 (즉, 아이디가 이미 존재하면)
        else {
            return false
        }
    }

    // 사용하려는 닉네임이 존재하는지 확인하는 메서드
    suspend fun checkJoinUserNickName(userNickName:String) : Boolean{
        // 닉네임을 통해 사용자 정보를 가져온다.
        val userVoList = userRepository.selectUserDataByUserNickName(userNickName)
        // 가져온 데이터가 있다면
        if(userVoList.isNotEmpty()){
            return false
        }
        // 가져온 데이터가 없다면
        else {
            return true
        }
    }

    // 사용자 정보를 추가하는 메서드
    // userDocId 리턴받이 프로필 이미지 변경할때 사용한다.
    fun addUserData(userModel: UserModel) : String{
        // 데이터를 VO에 담아준다.
        val userVO = userModel.toUserVO()
        // 저장하는 메서드를 호출한다.
        return userRepository.addUserData(userVO)
    }

    // 로그인 처리 메서드
    suspend fun checkLogin(loginUserId:String, loginUserPw:String) : LoginResult {
        // 로그인 결과
        var result = LoginResult.LOGIN_RESULT_SUCCESS

        // 입력한 아이디로 사용자 정보를 가져온다.
        val userVo = userRepository.selectUserDataByUserId(loginUserId)


        // 가져온 사용자 정보가 없다면
        if(userVo == null){
            result = LoginResult.LOGIN_RESULT_ID_NOT_EXIST
        } else {
            if(loginUserPw != userVo.userPw){
                // 비밀번호가 다르다면
                result = LoginResult.LOGIN_RESULT_PASSWORD_INCORRECT
            }
            // 탈퇴한 회원이라면
            if(userVo.userState == UserState.USER_STATE_SIGN_OUT.number){
                result = LoginResult.LOGIN_RESULT_SIGN_OUT_MEMBER
            }
        }
        return result
    }

    // 사용자 아이디를 통해 문서 id와 사용자 정보를 가져온다.
    // 사용자 아이디와 동일한 사용자의 정보 하나를 반환하는 메서드
    suspend fun selectUserDataByUserIdOne(userId:String) : UserModel?{
        val userVo = userRepository.selectUserDataByUserIdOne(userId)

        val loginUserModel = userVo?.toUserModel()

        return loginUserModel
    }

    // 자동로그인 토큰값을 갱신하는 메서드
    suspend fun updateUserAutoLoginToken(context: Context, userDocumentId:String){
        // 새로운 토큰값을 발행한다.
        val newToken = "${userDocumentId}${System.nanoTime()}"
        // SharedPreference에 저장한다.
        val pref = context.getSharedPreferences("LoginToken", Context.MODE_PRIVATE)
        pref.edit {
            putString("token", newToken)
        }
        // 서버에 저장한다.
        userRepository.updateUserAutoLoginToken(userDocumentId, newToken)
    }
    // 자동 로그인 토큰 값으로 사용자 정보를 가져오는 메서드
    suspend fun selectUserDataByLoginToken(loginToken:String) : UserModel?{
        val loginMap = userRepository.selectUserDataByLoginToken(loginToken)
        if(loginMap == null){
            return null
        } else {
            val userVO = loginMap["userVO"] as UserVO

            val userModel = userVO.toUserModel()
            return userModel
        }
    }

    // 카카오 로그인 토큰 값으로 사용자 정보를 가져오는 메서드
    suspend fun selectUserDataByKakaoLoginToken(kToken:Long) : UserModel?{
        val userVO = userRepository.selectUserDataByKakaoLoginToken(kToken)

        return userVO?.toUserModel()
    }

    // 사용자 데이터를 수정한다.
    suspend fun updateUserData(userModel: UserModel){
        val userVO = userModel.toUserVO()
        userRepository.updateUserData(userVO)
    }

    suspend fun updateUserLikeList(userDocId: String, userLikeList: List<String>) {
        userRepository.updateUserLikeList(userDocId, userLikeList)
    }
    // 사용자의 상태를 변경하는 메서드
    suspend fun updateUserState(userDocumentId:String, newState:UserState){
        userRepository.updateUserState(userDocumentId, newState)
    }

    // 이미지 데이터를 서버로 업로드 하는 메서드
    suspend fun uploadImage(sourceFilePath:String, serverFilePath:String){
        userRepository.uploadImage(sourceFilePath, serverFilePath)
    }
    // 유저Model 가져오는 메서드
    suspend fun getUserByUserDocId(userDocId: String): UserModel {
        Log.d("userDocId","userDocId :${userDocId}")
        val result = userRepository.getUserByUserDocId(userDocId)
        Log.d("getUserByUserDocId","result : $result")
        return result!!.toUserModel()
    }

    // 이미지 데이터 Uri 가져오기
    suspend fun gettingImage(imageFileName:String) : Uri {
        val imageUri = userRepository.gettingImage(imageFileName)
        val basicImage = "https://firebasestorage.googleapis.com/v0/b/wandertripsole.firebasestorage.app/o/basic%2FbasicProfileImage.png?alt=media&token=1463ed5d-c336-4c09-a214-adafe27d68b8"
        val basicImageUri: Uri = Uri.parse(basicImage)
        return imageUri?: basicImageUri
    }

    // 좋아요 목록 가져오기
    suspend fun gettingUserLikeList(userDocId: String): List<String> {
        return userRepository.getUserLikeList(userDocId)
    }

    // 사용자의 관심 콘텐츠 ID 리스트를 가져오는 함수
    suspend fun gettingUserInterestingContentIdList(userDocId: String): List<String> {
        return userRepository.gettingUserInterestingContentIdList(userDocId)
    }

    // 관심 지역 추가
    suspend fun addLikeItem(userDocId: String, likeItemContentId: String) {
        userRepository.addLikeItem(userDocId, likeItemContentId)
    }

    // 관심 지역(또는 콘텐츠) 카운트 증가
    suspend fun addLikeCnt(likeItemContentId: String) {
        userRepository.addLikeCnt(likeItemContentId)
    }

    // 관심 지역 삭제
    suspend fun removeLikeItem(userDocId: String, likeItemContentId: String) {
        userRepository.removeLikeItem(userDocId, likeItemContentId)
    }

    // 관심 지역(또는 콘텐츠) 카운트 감소
    suspend fun removeLikeCnt(likeItemContentId: String) {
        userRepository.removeLikeCnt(likeItemContentId)
    }

    // 해당 컨텐츠가 관심목록에 있는가?
    suspend fun isLikeContent(userDocId: String ,contentId : String): Boolean {
        var result = false
        val userLikeContentList = userRepository.gettingUserInterestingContentIdList(userDocId)

        if(userLikeContentList.contains(contentId)) result = true

        return result
    }

 /*   // 사용자의 일정 리스트에서 문서 아이디 리스트 가져오기
    suspend fun gettingUserScheduleIdList(userDocId: String): List<String> {
        return userRepository.gettingUserScheduleIdList(userDocId)
    }*/

    // 유저컬렉션에 스케줄 서브컬렉션 생성 메서드
    suspend fun addTripScheduleToUserSubCollection(userDocId: String, tripScheduleDocId: String) {
        userRepository.addTripScheduleToUserSubCollection(userDocId,tripScheduleDocId)
    }

    // 유저컬렉션에서 스케줄 서브컬렉션의 스케줄 문서 아이디 가져오기
    suspend fun gettingTripScheduleItemList(userDocId: String): List<String> {
        return userRepository.gettingTripScheduleItemList(userDocId)
    }

    // 유저컬렉션에서 스케줄 서브컬렉션의 스케줄 문서 삭제
    suspend fun deleteTripScheduleItem(userDocId: String, tripScheduleDocId: String) {
        return userRepository.deleteTripScheduleItem(userDocId,tripScheduleDocId)
    }

    // 유저 좋아요 목록 플로우 가져오기
    fun getUserLikeListFlow(userDocId: String): Flow<List<String>> {
        return userRepository.getUserLikeListFlow(userDocId)
    }

    // 유저 좋아요 목록 가져오기
    suspend fun getUserLikeListOnce(userDocId: String): List<String> {
        return userRepository.getUserLikeListOnce(userDocId)
    }

}