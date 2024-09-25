package com.kova700.bookchat.core.database.chatting.external.user.mapper

import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.database.chatting.external.user.model.UserEntity

fun User.toUserEntity(): UserEntity {
	return UserEntity(
		id = id,
		nickname = nickname,
		profileImageUrl = profileImageUrl,
		defaultProfileImageType = defaultProfileImageType
	)
}

fun List<User>.toUserEntity() = this.map { it.toUserEntity() }

fun UserEntity.toUser(): User {
	return User(
		id = id,
		nickname = nickname,
		profileImageUrl = profileImageUrl,
		defaultProfileImageType = defaultProfileImageType
	)
}

fun List<UserEntity>.toUser() = this.map { it.toUser() }