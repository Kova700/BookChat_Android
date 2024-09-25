package com.kova700.bookchat.core.network.bookchat.user

import com.kova700.bookchat.core.network.bookchat.user.model.response.UserResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

	@GET("/v1/api/members")
	suspend fun getUser(
		@Query("memberId") memberId: Long,
	): UserResponse
}