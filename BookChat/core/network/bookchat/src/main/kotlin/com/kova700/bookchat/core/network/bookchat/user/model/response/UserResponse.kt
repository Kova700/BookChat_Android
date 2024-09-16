package com.kova700.bookchat.core.network.bookchat.user.model.response

import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import com.google.gson.annotations.SerializedName

data class UserResponse(
	@SerializedName("userId")
	val userId: Long,
	@SerializedName("userNickname")
	val userNickname: String,
	@SerializedName("userProfileImageUri")
	val userProfileImageUri: String?,
	@SerializedName("defaultProfileImageType")
	val defaultProfileImageType: UserDefaultProfileTypeNetwork,
)