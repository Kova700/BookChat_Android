package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork
import com.example.bookchat.domain.model.UserDefaultProfileType

fun com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.toUserDefaultProfileType(): UserDefaultProfileType {
	return when (this) {
		com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.ONE -> UserDefaultProfileType.ONE
		com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.TWO -> UserDefaultProfileType.TWO
		com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.THREE -> UserDefaultProfileType.THREE
		com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.FOUR -> UserDefaultProfileType.FOUR
		com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.FIVE -> UserDefaultProfileType.FIVE
	}
}

fun UserDefaultProfileType.toUserDefaultProfileTypeNetwork(): com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork {
	return when (this) {
		UserDefaultProfileType.ONE -> com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.ONE
		UserDefaultProfileType.TWO -> com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.TWO
		UserDefaultProfileType.THREE -> com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.THREE
		UserDefaultProfileType.FOUR -> com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.FOUR
		UserDefaultProfileType.FIVE -> com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork.FIVE
	}
}
