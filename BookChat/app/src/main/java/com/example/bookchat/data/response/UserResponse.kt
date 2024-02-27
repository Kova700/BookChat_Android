package com.example.bookchat.data.response

import com.example.bookchat.domain.model.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName

data class UserResponse(
	@SerializedName("userId")
	val userId: Long,
	@SerializedName("userNickname")
	val userNickname: String,
	@SerializedName("userProfileImageUri")
	val userProfileImageUri: String?,
	@SerializedName("defaultProfileImageType")
	val defaultProfileImageType: UserDefaultProfileImageType,
	@SerializedName("userEmail")
	val userEmail: String? = null, // 필요한가..?
)