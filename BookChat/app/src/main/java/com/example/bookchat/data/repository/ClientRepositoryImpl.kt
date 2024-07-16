package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toBookChatToken
import com.example.bookchat.data.mapper.toNetWork
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.mapper.toUser
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestChangeUserNickname
import com.example.bookchat.data.network.model.request.RequestUserSignUp
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.FCMTokenRepository
import com.example.bookchat.domain.repository.OAuthIdTokenRepository
import com.example.bookchat.utils.toMultiPartBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import java.io.IOException
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val fcmTokenRepository: FCMTokenRepository,
	private val oAuthIdTokenRepository: OAuthIdTokenRepository,
) : ClientRepository {
	private val client = MutableStateFlow<User?>(null)

	override fun getClientFlow(): Flow<User> {
		return client.asStateFlow().filterNotNull()
	}

	override suspend fun isSignedIn(): Boolean {
		return bookChatTokenRepository.isBookChatTokenExist()
	}

	//TODO : userProfile = null로 보내면 null로 설정이 안됨 (서버 수정 대기중)
	override suspend fun changeClientProfile(
		newNickname: String,
		userProfile: ByteArray?,
	): User {
		bookChatApi.changeUserProfile(
			userProfileImage = userProfile?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = PROFILE_IMAGE_MULTIPART_NAME,
				fileName = PROFILE_IMAGE_FILE_NAME,
				fileExtension = PROFILE_IMAGE_FILE_EXTENSION
			),
			requestChangeUserNickname = RequestChangeUserNickname(nickname = newNickname)
		)

		return bookChatApi.getUserProfile().toUser()
			.also { client.update { it } }
	}

	override suspend fun renewBookChatToken(): BookChatToken {
		val refreshToken = bookChatTokenRepository.getBookChatToken()?.refreshToken
			?: throw IOException("Refresh Token does not exist.")
		val newToken = bookChatApi.renewBookChatToken(refreshToken).toBookChatToken()
		bookChatTokenRepository.saveBookChatToken(newToken)
		return newToken
	}

	override suspend fun signOut() {
		//Server FCM토큰 삭제 or logout API 호출
		bookChatTokenRepository.clearBookChatToken()
		//로컬에 FCM 토큰, 북챗 토큰, Room, DataStore 초기화
	}

	//TODO :회원 탈퇴 후 재가입 가능 기간 정책 결정해야함
	override suspend fun withdraw() {
		bookChatApi.withdraw()
		//TODO : 회원탈퇴해도 채팅방에 유저로 남아있는 현상이 있음 (부방장은 이상태지만 만약 방장이 이상태라면?)
		//TODO : 그리고 이 상태로 다시 가입하기하면 400날라오면서 이미 가입된 유저입니다 넘어오고 있음
		// <-- 400 https://bookchat.link/v1/api/users/signup (256ms)
		// {"errorCode":"4000100","message":"이미 가입된 사용자입니다."}
		//TODO : FCM은 또 이전 계정으로 계속 받아지고 있음 ,,,,미친,,
	}

	override suspend fun getClientProfile(): User {
		return client.firstOrNull()
			?: bookChatApi.getUserProfile().toUser()
				.also { client.emit(it) }
	}

	override suspend fun isDuplicatedUserNickName(nickName: String): Boolean {
		val response = bookChatApi.requestNameDuplicateCheck(nickName)
		return when (response.code()) {
			200 -> false
			409 -> true
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun renewFCMToken(fcmToken: FCMToken) {
		fcmTokenRepository.renewFCMToken(fcmToken)
	}

	override fun getCachedIdToken(): IdToken {
		return oAuthIdTokenRepository.getIdToken()
	}

	override fun saveIdToken(token: IdToken) {
		oAuthIdTokenRepository.saveIdToken(token)
	}

	override suspend fun clear() {
		client.update { null }
		//DB, DataStore, Repository에 있는 InMemoryData 전부 clear
		//1. 하나하나 전부 clear
		//2. 앱 데이터 전부 지우는 함수 하나 있다면 그거 찾아서 호출
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

	companion object {
		const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
		const val PROFILE_IMAGE_FILE_NAME = "profile_img"
		const val PROFILE_IMAGE_FILE_EXTENSION = ".webp"
		const val PROFILE_IMAGE_MULTIPART_NAME = "userProfileImage"
	}
}