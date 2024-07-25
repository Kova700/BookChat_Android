package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.OAuth2ProviderNetwork
import com.example.bookchat.oauth.oauthclient.external.model.OAuth2Provider

fun OAuth2ProviderNetwork.toDomain(): OAuth2Provider {
	return OAuth2Provider.valueOf(name)
}

fun OAuth2Provider.toNetwork(): OAuth2ProviderNetwork {
	return OAuth2ProviderNetwork.valueOf(name)
}