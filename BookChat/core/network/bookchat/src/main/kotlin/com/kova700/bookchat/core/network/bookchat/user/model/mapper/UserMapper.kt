package com.kova700.bookchat.core.network.bookchat.user.model.mapper

import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.user.model.response.UserResponse

fun UserResponse.toUser(): User {
	return User(
		id = userId,
		nickname = userNickname,
		profileImageUrl = userProfileImageUri,
		defaultProfileImageType = defaultProfileImageType.toDomain()
	)
}