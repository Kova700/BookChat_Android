package com.kova700.bookchat.core.data.user.internal.mapper

import com.kova700.bookchat.core.network.bookchat.model.both.UserDefaultProfileTypeNetwork
import com.kova700.bookchat.core.data.user.external.model.UserDefaultProfileType

fun UserDefaultProfileTypeNetwork.toDomain(): UserDefaultProfileType {
	return UserDefaultProfileType.valueOf(name)
}

fun UserDefaultProfileType.toNetwork(): UserDefaultProfileTypeNetwork {
	return UserDefaultProfileTypeNetwork.valueOf(name)
}
