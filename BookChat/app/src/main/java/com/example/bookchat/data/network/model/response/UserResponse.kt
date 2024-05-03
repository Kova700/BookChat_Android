package com.example.bookchat.data.network.model.response

import com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork
import com.google.gson.annotations.SerializedName

data class UserResponse(
	@SerializedName("userId")
	val userId: Long,
	@SerializedName("userNickname")
	val userNickname: String,
	@SerializedName("userProfileImageUri")
	val userProfileImageUri: String?,
	@SerializedName("defaultProfileImageType")
	val defaultProfileImageType: com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork,
)