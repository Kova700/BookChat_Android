package com.example.bookchat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.App
import com.example.bookchat.data.datastore.clearData
import com.example.bookchat.data.datastore.getDataFlow
import com.example.bookchat.data.datastore.setData
import com.example.bookchat.data.mapper.toBookChatToken
import com.example.bookchat.data.mapper.toNetWork
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
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.toMultiPartBody
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

class ClientRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val dataStore: DataStore<Preferences>,
	private val bookChatTokenRepository: BookChatTokenRepository,
) : ClientRepository,
	BookChatTokenRepository by bookChatTokenRepository {

	private val deviceUUIDKey = stringPreferencesKey(DEVICE_UUID_KEY)

	private var cachedClient: User? = null
	private var cachedIdToken: IdToken? = null

	override suspend fun signIn(
		approveChangingDevice: Boolean
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestUserSignIn = RequestUserSignIn(
			fcmToken = getFCMToken().text,
			deviceToken = getDeviceID(),
			approveChangingDevice = approveChangingDevice,
			oauth2Provider = cachedIdToken!!.oAuth2Provider.toOAuth2ProviderNetwork()
		)
		val response = bookChatApi.signIn(cachedIdToken!!.token, requestUserSignIn)
		when (response.code()) {
			200 -> {
				val token = response.body()
				token?.let {
					saveBookChatToken(token.toBookChatToken())
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

	override suspend fun signUp(
		nickname: String,
		readingTastes: List<ReadingTaste>,
		userProfile: ByteArray?
	) {
		if (isNetworkConnected().not()) throw NetworkIsNotConnectedException()
		val idToken = cachedIdToken ?: throw IOException("IdToken does not exist.")

		val requestUserSignUp = RequestUserSignUp(
			oauth2Provider = idToken.oAuth2Provider.toOAuth2ProviderNetwork(),
			nickname = nickname,
			readingTastes = readingTastes.map { it.toNetWork() },
//			defaultProfileImageType = ???????이거도 그럼 처음부터 가져와야하는거아닌가?
		)
		bookChatApi.signUp(
			idToken = idToken.token,
			userProfileImage = userProfile?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = PROFILE_IMAGE_MULTIPART_NAME,
				fileName = PROFILE_IMAGE_FILE_NAME,
				fileExtension = PROFILE_IMAGE_FILE_EXTENSION
			),
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

	override suspend fun renewFCMToken(fcmToken: FCMToken) {
		if (isBookChatTokenExist().not()) return
		//TODO : 매번 로그인 시에 호출하여 서버에 등록된 FCM 토큰 덮어쓰기
		// 가장 최근 기기로 알람가게 구현
	}

	private suspend fun getFCMToken(): FCMToken {
		val fcmToken = suspendCancellableCoroutine<Result<String>> { continuation ->
			FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
				if (task.isSuccessful.not()) return@addOnCompleteListener
				continuation.resume(Result.success(task.result))
			}
		}.map { FCMToken(it) }.getOrElse { e -> throw e }
		return fcmToken
	}

	private suspend fun getDeviceID(): String {
		return dataStore.getDataFlow(deviceUUIDKey).firstOrNull() ?: getNewDeviceID()
	}

	override fun getCachedIdToken(): IdToken {
		return cachedIdToken!!
	}

	override fun saveIdToken(token: IdToken) {
		cachedIdToken = token
	}

	private suspend fun getNewDeviceID(): String {
		val uuid = UUID.randomUUID().toString()
		dataStore.setData(deviceUUIDKey, uuid)
		return uuid
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
		private const val DEVICE_UUID_KEY = "DEVICE_UUID_KEY"

		const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
		const val PROFILE_IMAGE_FILE_NAME = "profile_img"
		const val PROFILE_IMAGE_FILE_EXTENSION = ".webp"
		const val PROFILE_IMAGE_MULTIPART_NAME = "userProfileImage"
	}
}