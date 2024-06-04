package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork
import com.example.bookchat.domain.model.UserDefaultProfileType

fun UserDefaultProfileTypeNetwork.toDomain(): UserDefaultProfileType {
	return UserDefaultProfileType.valueOf(name)
}

fun UserDefaultProfileType.toNetwork(): UserDefaultProfileTypeNetwork {
	return UserDefaultProfileTypeNetwork.valueOf(name)
}
