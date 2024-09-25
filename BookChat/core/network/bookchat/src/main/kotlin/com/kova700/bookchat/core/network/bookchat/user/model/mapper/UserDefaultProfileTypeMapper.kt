package com.kova700.bookchat.core.network.bookchat.user.model.mapper

import com.kova700.bookchat.core.data.user.external.model.UserDefaultProfileType
import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork

fun UserDefaultProfileTypeNetwork.toDomain(): UserDefaultProfileType {
	return UserDefaultProfileType.valueOf(name)
}

fun UserDefaultProfileType.toNetwork(): UserDefaultProfileTypeNetwork {
	return UserDefaultProfileTypeNetwork.valueOf(name)
}
