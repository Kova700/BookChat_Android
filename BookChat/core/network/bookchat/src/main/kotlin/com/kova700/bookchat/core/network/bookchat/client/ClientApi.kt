package com.kova700.bookchat.core.network.bookchat.client

import com.kova700.bookchat.core.data.util.model.network.BookChatApiResult
import com.kova700.bookchat.core.network.bookchat.client.model.request.RequestChangeUserNickname
import com.kova700.bookchat.core.network.bookchat.client.model.request.RequestUserLogin
import com.kova700.bookchat.core.network.bookchat.client.model.request.RequestUserSignUp
import com.kova700.bookchat.core.network.bookchat.client.model.response.BookChatTokenResponse
import com.kova700.bookchat.core.network.bookchat.user.model.response.UserResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ClientApi {

	@GET("/v1/api/users/profile/nickname")
	suspend fun requestNameDuplicateCheck(
		@Query("nickname") nickname: String,
	): BookChatApiResult<Unit>

	@Multipart
	@POST("/v1/api/users/signup")
	suspend fun signUp(
		@Header("OIDC") idToken: String,
		@Part userProfileImage: MultipartBody.Part? = null,
		@Part("userSignUpRequest") requestUserSignUp: RequestUserSignUp,
	)

	@PUT("/v1/api/devices/fcm-token")
	suspend fun renewFcmToken(
		@Body fcmToken: String,
	)

	@Multipart
	@POST("/v1/api/users/profile")
	suspend fun changeUserProfile(
		@Part userProfileImage: MultipartBody.Part? = null,
		@Part("changeUserNicknameRequest") requestChangeUserNickname: RequestChangeUserNickname,
	)

	@POST("/v1/api/users/signin")
	suspend fun login(
		@Header("OIDC") idToken: String,
		@Body requestUserLogin: RequestUserLogin,
	): BookChatApiResult<BookChatTokenResponse>

	@POST("/v1/api/users/logout")
	suspend fun logout()

	@POST("/v1/api/auth/token")
	suspend fun renewBookChatToken(
		@Body refreshToken: String,
	): BookChatTokenResponse

	@DELETE("/v1/api/users")
	suspend fun withdraw()

	@GET("/v1/api/users/profile")
	suspend fun getUserProfile(): UserResponse

}