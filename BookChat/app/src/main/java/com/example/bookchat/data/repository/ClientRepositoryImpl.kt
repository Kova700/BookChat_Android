package com.example.bookchat.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.data.datastore.clearData
import com.example.bookchat.data.datastore.getDataFlow
import com.example.bookchat.data.datastore.setData
import com.example.bookchat.data.mapper.toBookChatToken
import com.example.bookchat.data.mapper.toNetWork
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.mapper.toUser
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestChangeUserNickname
import com.example.bookchat.data.network.model.request.RequestUserSignIn
import com.example.bookchat.data.network.model.request.RequestUserSignUp
import com.example.bookchat.data.network.model.response.NeedToDeviceWarningException
import com.example.bookchat.data.network.model.response.NeedToSignUpException
import com.example.bookchat.data.network.model.response.ResponseBodyEmptyException
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.toMultiPartBody
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

//TODO : BookChatToken, FCMToken , DeviceUUID Repository 구분필요
//TODO : 로그아웃, 회원탈퇴 때문에 Repository전부 주입으로 여기로 들어와야할듯?
class ClientRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val dataStore: DataStore<Preferences>,
	private val bookChatTokenRepository: BookChatTokenRepository,
) : ClientRepository {
	private val deviceUUIDKey = stringPreferencesKey(DEVICE_UUID_KEY)
	private var cachedIdToken: IdToken? = null
	private val client = MutableStateFlow<User?>(null)

	override fun getClientFlow(): Flow<User> {
		return client.asStateFlow().filterNotNull()
	}

	override suspend fun isSignedIn(): Boolean {
		return bookChatTokenRepository.isBookChatTokenExist()
	}

	override suspend fun signIn(
		approveChangingDevice: Boolean,
	) {
		val requestUserSignIn = RequestUserSignIn(
			fcmToken = getFCMToken().text,
			deviceToken = getDeviceID(),
			approveChangingDevice = approveChangingDevice,
			oauth2Provider = cachedIdToken!!.oAuth2Provider.toNetwork()
		)
		val response = bookChatApi.signIn(cachedIdToken!!.token, requestUserSignIn)
		when (response.code()) {
			200 -> {
				val token = response.body()
				token?.let {
					bookChatTokenRepository.saveBookChatToken(token.toBookChatToken())
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
		userProfile: ByteArray?,
	) {
		val idToken = cachedIdToken ?: throw IOException("IdToken does not exist.")

		val requestUserSignUp = RequestUserSignUp(
			oauth2Provider = idToken.oAuth2Provider.toNetwork(),
			nickname = nickname,
			readingTastes = readingTastes.map { it.toNetWork() },
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

	//TODO : userProfile = null로 보내면 null로 설정이 안됨 (서버 수정 대기중)
	override suspend fun changeClientProfile(
		newNickname: String,
		userProfile: ByteArray?,
	): User {
		Log.d(TAG, "ClientRepositoryImpl: changeClientProfile() - called")
		bookChatApi.changeUserProfile(
			userProfileImage = userProfile?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = PROFILE_IMAGE_MULTIPART_NAME,
				fileName = PROFILE_IMAGE_FILE_NAME,
				fileExtension = PROFILE_IMAGE_FILE_EXTENSION
			),
			requestChangeUserNickname = RequestChangeUserNickname(nickname = newNickname)
		)

		/** 갱신을 위해 자체적으로 다시 호출 */
		return bookChatApi.getUserProfile().toUser()
			.also { client.emit(it) }
	}

	override suspend fun renewBookChatToken(): BookChatToken? {
		val refreshToken = bookChatTokenRepository.getBookChatToken()?.refreshToken ?: return null
		val newToken = bookChatApi.renewBookChatToken(refreshToken).toBookChatToken()
		bookChatTokenRepository.saveBookChatToken(newToken)
		return newToken
	}

	override suspend fun signOut(needAServer: Boolean) {
		if (needAServer) {
			//Server FCM토큰 삭제 or logout API 호출
		}
		bookChatTokenRepository.clearBookChatToken()
		//로컬에 FCM 토큰, 북챗 토큰, Room, DataStore 초기화
	}

	//TODO :회원 탈퇴 후 재가입 가능 기간 정책 결정해야함
	override suspend fun withdraw() {
		bookChatApi.withdraw()
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
		if (bookChatTokenRepository.isBookChatTokenExist().not()) return
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

	companion object {
		private const val DEVICE_UUID_KEY = "DEVICE_UUID_KEY"

		const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
		const val PROFILE_IMAGE_FILE_NAME = "profile_img"
		const val PROFILE_IMAGE_FILE_EXTENSION = ".webp"
		const val PROFILE_IMAGE_MULTIPART_NAME = "userProfileImage"
	}
}