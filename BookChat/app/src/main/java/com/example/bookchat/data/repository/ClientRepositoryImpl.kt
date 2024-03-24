package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.mapper.toUser
import com.example.bookchat.data.request.RequestUserSignIn
import com.example.bookchat.data.request.RequestUserSignUp
import com.example.bookchat.data.response.NeedToDeviceWarningException
import com.example.bookchat.data.response.NeedToSignUpException
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.NickNameDuplicateException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.DataStoreManager
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : ClientRepository {

	private var cachedClient: User? = null

	override suspend fun signIn(approveChangingDevice: Boolean) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val idToken = DataStoreManager.getIdToken()
		val fcmToken = DataStoreManager.getFCMToken()
		val deviceID = DataStoreManager.getDeviceID()
		val requestUserSignIn = RequestUserSignIn(
			fcmToken, deviceID, approveChangingDevice, idToken.oAuth2Provider
		)

		val response = bookChatApi.signIn(idToken.token, requestUserSignIn)
		when (response.code()) {
			200 -> {
				val token = response.body()
				token?.let { DataStoreManager.saveBookChatToken(token); return }
				throw ResponseBodyEmptyException(response.errorBody()?.string())
			}

			404 -> throw NeedToSignUpException(response.errorBody()?.string())
			409 -> throw NeedToDeviceWarningException(response.errorBody()?.string())
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun signUp(userSignUpDto: UserSignUpDto) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val idToken = DataStoreManager.getIdToken()
		val requestUserSignUp = RequestUserSignUp(
			oauth2Provider = idToken.oAuth2Provider,
			nickname = userSignUpDto.nickname,
			readingTastes = userSignUpDto.readingTastes
		)

		val response = bookChatApi.signUp(
			idToken = idToken.token,
			userProfileImage = userSignUpDto.userProfileImage,
			requestUserSignUp = requestUserSignUp
		)

		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun signOut(needAServer: Boolean) {
		if (needAServer) {
			//Server FCM토큰 삭제 or logout API 호출
		}
		DataStoreManager.deleteBookChatToken()
		//로컬에 FCM 토큰, 북챗 토큰, Room, DataStore 초기화
	}

	//회원 탈퇴 후 재가입 가능 기간 정책 결정해야함
	override suspend fun withdraw() {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		val response = bookChatApi.withdraw()
		when (response.code()) {
			200 -> signOut()
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun getClientProfile(): User {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		cachedClient?.let { return it }
		return bookChatApi.getUserProfile().toUser().also { cachedClient = it }
	}

	override suspend fun checkForDuplicateUserName(nickName: String) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.requestNameDuplicateCheck(nickName)
		when (response.code()) {
			200 -> {}
			409 -> throw NickNameDuplicateException(response.errorBody()?.string())
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun renewFCMToken(token: String) {
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}
}

