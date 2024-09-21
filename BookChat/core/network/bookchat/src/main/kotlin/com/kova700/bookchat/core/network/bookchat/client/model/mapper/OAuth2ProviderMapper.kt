package com.kova700.bookchat.core.network.bookchat.client.model.mapper

import com.kova700.bookchat.core.network.bookchat.client.model.both.OAuth2ProviderNetwork
import com.kova700.bookchat.core.oauth.external.model.OAuth2Provider

fun OAuth2ProviderNetwork.toDomain(): OAuth2Provider {
	return OAuth2Provider.valueOf(name)
}

fun OAuth2Provider.toNetwork(): OAuth2ProviderNetwork {
	return OAuth2ProviderNetwork.valueOf(name)
}