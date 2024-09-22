package com.kova700.bookchat.core.network.bookchat.user.model.response

import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO: 서버로부터 UserEmail 필드가 넘어옴 (서버 수정 필요)
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