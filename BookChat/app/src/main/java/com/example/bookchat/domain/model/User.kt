package com.example.bookchat.domain.model

data class User(
	val id: Long,
	val nickname: String,
	val profileImageUrl: String?,
	val defaultProfileImageType: UserDefaultProfileType
) {
	companion object {
		val Default = User(
			id = -1L,
			nickname = "",
			profileImageUrl = null,
			defaultProfileImageType = UserDefaultProfileType.ONE
		)
	}
}
