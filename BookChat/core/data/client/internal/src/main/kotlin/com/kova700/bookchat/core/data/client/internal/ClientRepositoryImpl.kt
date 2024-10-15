package com.kova700.bookchat.core.data.client.internal

import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.client.external.model.AlreadySignedUpException
import com.kova700.bookchat.core.data.client.external.model.NeedToDeviceWarningException
import com.kova700.bookchat.core.data.client.external.model.NeedToSignUpException
import com.kova700.bookchat.core.data.client.external.model.ReadingTaste
import com.kova700.bookchat.core.data.common.model.network.BookChatApiResult
import com.kova700.bookchat.core.data.fcm_token.external.model.FCMToken
import com.kova700.bookchat.core.data.oauth.external.model.IdToken
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.channel.model.response.BookChatFailResponseBody
import com.kova700.bookchat.core.network.bookchat.client.ClientApi
import com.kova700.bookchat.core.network.bookchat.client.model.mapper.toBookChatToken
import com.kova700.bookchat.core.network.bookchat.client.model.mapper.toNetWork
import com.kova700.bookchat.core.network.bookchat.client.model.mapper.toNetwork
import com.kova700.bookchat.core.network.bookchat.client.model.request.RequestChangeUserNickname
import com.kova700.bookchat.core.network.bookchat.client.model.request.RequestUserLogin
import com.kova700.bookchat.core.network.bookchat.client.model.request.RequestUserSignUp
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toUser
import com.kova700.bookchat.core.network.util.multipart.toMultiPartBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
	private val clientApi: ClientApi,
	private val jsonSerializer: Json,
) : ClientRepository {
	private val client = MutableStateFlow<User?>(null)

	override fun getClientFlow(): Flow<User> {
		return client.asStateFlow().filterNotNull()
	}

	override suspend fun login(
		idToken: IdToken,
		fcmToken: FCMToken,
		deviceUUID: String,
		isDeviceChangeApproved: Boolean,
	): BookChatToken {
		val requestUserLogin = RequestUserLogin(
			fcmToken = fcmToken.text,
			deviceToken = deviceUUID,
			isDeviceChangeApproved = isDeviceChangeApproved,
			oauth2Provider = idToken.oAuth2Provider.toNetwork()
		)

		val response = clientApi.login(idToken.token, requestUserLogin)
		return when (response) {
			is BookChatApiResult.Success -> response.data.toBookChatToken()
			is BookChatApiResult.Failure -> {
				when (response.code) {
					404 -> throw NeedToSignUpException(response.body)
					409 -> throw NeedToDeviceWarningException(response.body)
					else -> throw Exception(
						createExceptionMessage(
							response.code,
							response.body
						)
					)
				}
			}
		}

	}

	override suspend fun signUp(
		idToken: IdToken,
		nickname: String,
		readingTastes: List<ReadingTaste>,
		userProfile: ByteArray?,
	) {
		val requestUserSignUp = RequestUserSignUp(
			oauth2Provider = idToken.oAuth2Provider.toNetwork(),
			nickname = nickname,
			readingTastes = readingTastes.map { it.toNetWork() },
		)

		val response = clientApi.signUp(
			idToken = idToken.token,
			userProfileImage = userProfile?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = PROFILE_IMAGE_MULTIPART_NAME,
				fileName = PROFILE_IMAGE_FILE_NAME,
				fileExtension = PROFILE_IMAGE_FILE_EXTENSION
			),
			requestUserSignUp = requestUserSignUp
		)

		when (response) {
			is BookChatApiResult.Success -> Unit
			is BookChatApiResult.Failure -> {
				val failBody =
					response.body?.let { jsonSerializer.decodeFromString<BookChatFailResponseBody>(it) }
				when (failBody?.errorCode) {
					RESPONSE_CODE_ALREADY_SIGNED_UP -> throw AlreadySignedUpException(failBody.message)
					else -> throw IOException("failed to signUp")
				}
			}
		}
	}

	override suspend fun changeClientProfile(
		newNickname: String,
		isProfileChanged: Boolean,
		userProfile: ByteArray?,
	): User {
		clientApi.changeUserProfile(
			userProfileImage = userProfile?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = PROFILE_IMAGE_MULTIPART_NAME,
				fileName = PROFILE_IMAGE_FILE_NAME,
				fileExtension = PROFILE_IMAGE_FILE_EXTENSION
			),
			requestChangeUserNickname = RequestChangeUserNickname(
				nickname = newNickname,
				isProfileChanged = isProfileChanged
			)
		)
		return clientApi.getUserProfile().toUser()
			.also { newClient -> client.update { newClient } }
	}

	override suspend fun renewBookChatToken(currentToken: BookChatToken): BookChatToken {
		return clientApi.renewBookChatToken(currentToken.refreshToken).toBookChatToken()
	}

	/** LogoutUsecase를 이용해 로컬 데이터 삭제가 필요함으로 해당 함수 단일로 호출 금지
	 * (서버 FCM토큰 삭제용도로 사용)*/
	//TODO : [FixWaiting] DeviceID가져와서 서버에게 보내기( DeviceID가 같은 경우만 FCM 토큰 삭제되게 수정)
	override suspend fun logout() {
		clientApi.logout()
	}

	/** WithdrawUsecase를 이용해 로컬 데이터 삭제가 필요함으로 해당 함수 단일로 호출 금지 */
	override suspend fun withdraw() {
		clientApi.withdraw()
	}

	override suspend fun getClientProfile(): User {
		return client.firstOrNull()
			?: getClientProfileFromServer()
	}

	private suspend fun getClientProfileFromServer(): User {
		return clientApi.getUserProfile().toUser()
			.also { newClient -> client.update { newClient } }
	}

	override suspend fun isDuplicatedUserNickName(nickName: String): Boolean {
		val response = clientApi.requestNameDuplicateCheck(nickName)
		return when (response) {
			is BookChatApiResult.Success -> false
			is BookChatApiResult.Failure ->
				when (response.code) {
					409 -> true
					else -> throw Exception(
						createExceptionMessage(
							response.code,
							response.body
						)
					)
				}
		}
	}

	override suspend fun clear() {
		client.update { null }
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String? = ""): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

	companion object {
		const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
		const val PROFILE_IMAGE_FILE_NAME = "profile_img"
		const val PROFILE_IMAGE_FILE_EXTENSION = ".webp"
		const val PROFILE_IMAGE_MULTIPART_NAME = "userProfileImage"
	}
}