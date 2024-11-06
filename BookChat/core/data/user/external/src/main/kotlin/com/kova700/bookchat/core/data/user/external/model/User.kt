package com.kova700.bookchat.core.data.user.external.model

data class User(
	val id: Long,
	val nickname: String,
	val profileImageUrl: String?,
	val defaultProfileImageType: UserDefaultProfileType,
) {
	companion object {
		val DEFAULT = User(
			id = -1L,
			nickname = "",
			profileImageUrl = null,
			defaultProfileImageType = UserDefaultProfileType.ONE
		)
	}
}
