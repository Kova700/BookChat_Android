package com.example.bookchat.data.mapper

import com.example.bookchat.data.database.model.UserEntity
import com.example.bookchat.data.network.model.response.UserResponse
import com.example.bookchat.domain.model.User

fun User.toUserEntity(): UserEntity {
	return UserEntity(
		id = id,
		nickname = nickname,
		profileImageUrl = profileImageUrl,
		defaultProfileImageType = defaultProfileImageType
	)
}

fun UserEntity.toUser(): User {
	return User(
		id = id,
		nickname = nickname,
		profileImageUrl = profileImageUrl,
		defaultProfileImageType = defaultProfileImageType
	)
}

fun UserResponse.toUser(): User {
	return User(
		id = userId,
		nickname = userNickname,
		profileImageUrl = userProfileImageUri,
		defaultProfileImageType = defaultProfileImageType.toUserDefaultProfileType()
	)
}

fun UserResponse.toUserEntity(): UserEntity {
	return UserEntity(
		id = userId,
		nickname = userNickname,
		profileImageUrl = userProfileImageUri,
		defaultProfileImageType = defaultProfileImageType.toUserDefaultProfileType()
	)
}

fun List<UserEntity>.toUser() = this.map { it.toUser() }
fun List<User>.toUserEntity() = this.map { it.toUserEntity() }
