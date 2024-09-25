package com.kova700.bookchat.core.network.bookchat.user.model.both

import com.kova700.bookchat.core.network.network.converter.EnumAsIntSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(with = UserDefaultProfileTypeNetworkSerializer::class)
enum class UserDefaultProfileTypeNetwork(val code: Int) {
	ONE(1),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
}

class UserDefaultProfileTypeNetworkSerializer :
	KSerializer<UserDefaultProfileTypeNetwork> by EnumAsIntSerializer(
		codeGetter = { enum: UserDefaultProfileTypeNetwork -> enum.code },
		enumGetter = { code: Int ->
			UserDefaultProfileTypeNetwork.entries.first { it.code == code }
		}
	)