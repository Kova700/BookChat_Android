package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toBookChatToken
import com.example.bookchat.data.mapper.toNetWork
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.mapper.toUser
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestChangeUserNickname
import com.example.bookchat.data.network.model.request.RequestUserLogin
import com.example.bookchat.data.network.model.request.RequestUserSignUp
import com.example.bookchat.data.network.model.response.NeedToDeviceWarningException
import com.example.bookchat.data.network.model.response.NeedToSignUpException
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.oauth.external.model.IdToken
import com.example.bookchat.utils.toMultiPartBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
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

		val response = bookChatApi.login(idToken.token, requestUserLogin)
		if (response.isSuccessful) response.body()?.toBookChatToken()?.let { return it }
		when (response.code()) {
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
			.also { newClient -> client.update { newClient } }
	}

	override suspend fun renewBookChatToken(currentToken: BookChatToken): BookChatToken {
		return bookChatApi.renewBookChatToken(currentToken.refreshToken).toBookChatToken()
	}

	/** LogoutUsecase를 이용해 로컬 데이터 삭제가 필요함으로 해당 함수 단일로 호출 금지
	 * (서버 FCM토큰 삭제용도로 사용)*/
	override suspend fun logout() {
		bookChatApi.logout()
	}

	/** WithdrawUsecase를 이용해 로컬 데이터 삭제가 필요함으로 해당 함수 단일로 호출 금지 */
	//TODO :회원 탈퇴 후 재가입 가능 기간 정책 결정해야함
	//TODO : 회원탈퇴해도 채팅방에 유저로 남아있는 현상이 있음 (부방장은 이상태지만 만약 방장이 이상태라면?)
	//TODO : 그리고 이 상태로 다시 가입하기하면 400날라오면서 이미 가입된 유저입니다 넘어오고 있음
	// <-- 400 https://bookchat.link/v1/api/users/signup (256ms)
	// {"errorCode":"4000100","message":"이미 가입된 사용자입니다."}
	//TODO : FCM은 또 이전 계정으로 계속 받아지고 있음
	//TODO : 방장이 회원탈퇴해도 채팅방이 터지지 않고 남아있음
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

	override suspend fun clear() {
		client.update { null }
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