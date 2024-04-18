package com.example.bookchat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.App
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.data.datastore.clearData
import com.example.bookchat.data.datastore.getDataFlow
import com.example.bookchat.data.datastore.setData
import com.example.bookchat.data.mapper.toBookChatToken
import com.example.bookchat.data.mapper.toOAuth2ProviderNetwork
import com.example.bookchat.data.mapper.toUser
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.request.RequestUserSignIn
import com.example.bookchat.data.request.RequestUserSignUp
import com.example.bookchat.data.response.NeedToDeviceWarningException
import com.example.bookchat.data.response.NeedToSignUpException
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.NickNameDuplicateException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.DataStoreManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

class ClientRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val dataStore: DataStore<Preferences>,
	private val gson: Gson,
) : ClientRepository {
	//GoogleLoginRepository
	//KakaoLoginRepository
	private val bookChatTokenKey = stringPreferencesKey(BOOKCHAT_TOKEN_KEY)
	private val fcmTokenKey = stringPreferencesKey(FCM_TOKEN_KEY)
	private val deviceUUIDKey = stringPreferencesKey(DEVICE_UUID_KEY)

	private var cachedClient: User? = null

	override suspend fun signIn(
		idToken: IdToken,
		approveChangingDevice: Boolean
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestUserSignIn = RequestUserSignIn(
			fcmToken = getFCMToken().text,
			deviceToken = getDeviceID(),
			approveChangingDevice = approveChangingDevice,
			oauth2Provider = idToken.oAuth2Provider.toOAuth2ProviderNetwork()
		)

		val response = bookChatApi.signIn(idToken.token, requestUserSignIn)
		when (response.code()) {
			200 -> {
				val token = response.body()
				token?.let {
					DataStoreManager.saveBookChatToken(token.toBookChatToken())
					return
				}
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
			oauth2Provider = idToken.oAuth2Provider.toOAuth2ProviderNetwork(),
			nickname = userSignUpDto.nickname,
			readingTastes = userSignUpDto.readingTastes
		)

		bookChatApi.signUp(
			idToken = idToken.token,
			userProfileImage = userSignUpDto.userProfileImage,
			requestUserSignUp = requestUserSignUp
		)
	}

	override suspend fun signOut(needAServer: Boolean) {
		if (needAServer) {
			//Server FCM토큰 삭제 or logout API 호출
		}
		clearBookChatToken()
		//로컬에 FCM 토큰, 북챗 토큰, Room, DataStore 초기화
	}

	//TODO :회원 탈퇴 후 재가입 가능 기간 정책 결정해야함
	override suspend fun withdraw() {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		bookChatApi.withdraw()
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
		//TODO : 매번 로그인 시에 호출하여 서버에 등록된 FCM 토큰 덮어쓰기
		// 가장 최근 기기로 알람가게 구현

	}

	//TODO : 토큰 암호화 추가
	override suspend fun getBookChatToken(): BookChatToken? {
		val tokenString = dataStore.getDataFlow(bookChatTokenKey).firstOrNull()
		if (tokenString.isNullOrBlank()) return null
		return gson.fromJson(tokenString, BookChatToken::class.java)
	}

	//TODO : 없다면 API 호출해서 가져오기
	private suspend fun getFCMToken(): FCMToken {
		val cachedToken = dataStore.getDataFlow(fcmTokenKey).firstOrNull()
		if (cachedToken != null) return FCMToken(cachedToken)

		suspendCancellableCoroutine<Result<String>> { continuation ->
			FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
				if (task.isSuccessful.not()) throw IOException("Failed to retrieve FCMToken from Google servers.")
				continuation.resume(Result.success(task.result))
			}
		}.onSuccess { token ->
			val fcmToken = FCMToken(token)
			saveFCMToken(fcmToken)
			return fcmToken
		}
		throw IOException("Failed to retrieve FCMToken from Google servers.")
	}

	private suspend fun getDeviceID(): String {
		return dataStore.getDataFlow(deviceUUIDKey).firstOrNull() ?: getNewDeviceID()
	}

	override suspend fun saveBookChatToken(token: BookChatToken) {
		val tokenString = gson.toJson(
			token.copy(accessToken = "$BOOKCHAT_TOKEN_PREFIX ${token.accessToken}")
		)
		dataStore.setData(bookChatTokenKey, tokenString)
	}

	suspend fun saveFCMToken(fcmToken: FCMToken) {
		dataStore.setData(fcmTokenKey, fcmToken.text)
	}

	private suspend fun getNewDeviceID(): String {
		val uuid = UUID.randomUUID().toString()
		dataStore.setData(deviceUUIDKey, uuid)
		return uuid
	}

	private suspend fun clearBookChatToken() {
		dataStore.clearData(bookChatTokenKey)
	}

	private suspend fun clearFCMToken() {
		dataStore.clearData(fcmTokenKey)
	}

	private suspend fun clearDeviceUUIDKey() {
		dataStore.clearData(deviceUUIDKey)
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	companion object {
		private const val BOOKCHAT_TOKEN_PREFIX = "Bearer"
		private const val BOOKCHAT_TOKEN_KEY = "BOOKCHAT_TOKEN_KEY"
		private const val FCM_TOKEN_KEY = "FCM_TOKEN_KEY"
		private const val DEVICE_UUID_KEY = "DEVICE_UUID_KEY"
	}
}

