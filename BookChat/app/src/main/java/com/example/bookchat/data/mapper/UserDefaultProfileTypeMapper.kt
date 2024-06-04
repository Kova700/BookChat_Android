package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork
import com.example.bookchat.domain.model.UserDefaultProfileType

fun UserDefaultProfileTypeNetwork.toUserDefaultProfileType(): UserDefaultProfileType {
	return when (this) {
		UserDefaultProfileTypeNetwork.ONE -> UserDefaultProfileType.ONE
		UserDefaultProfileTypeNetwork.TWO -> UserDefaultProfileType.TWO
		UserDefaultProfileTypeNetwork.THREE -> UserDefaultProfileType.THREE
		UserDefaultProfileTypeNetwork.FOUR -> UserDefaultProfileType.FOUR
		UserDefaultProfileTypeNetwork.FIVE -> UserDefaultProfileType.FIVE
	}
}

fun UserDefaultProfileType.toUserDefaultProfileTypeNetwork(): UserDefaultProfileTypeNetwork {
	return when (this) {
		UserDefaultProfileType.ONE -> UserDefaultProfileTypeNetwork.ONE
		UserDefaultProfileType.TWO -> UserDefaultProfileTypeNetwork.TWO
		UserDefaultProfileType.THREE -> UserDefaultProfileTypeNetwork.THREE
		UserDefaultProfileType.FOUR -> UserDefaultProfileTypeNetwork.FOUR
		UserDefaultProfileType.FIVE -> UserDefaultProfileTypeNetwork.FIVE
	}
}
