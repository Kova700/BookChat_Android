package com.kova700.bookchat.core.network.bookchat.user.model.response

import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
	@SerialName("userId")
	val userId: Long,
	@SerialName("userNickname")
	val userNickname: String,
	@SerialName("userProfileImageUri")
	val userProfileImageUri: String? = null,
	@SerialName("defaultProfileImageType")
	val defaultProfileImageType: UserDefaultProfileTypeNetwork,
)