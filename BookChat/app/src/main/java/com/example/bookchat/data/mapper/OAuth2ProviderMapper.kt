package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.OAuth2ProviderNetwork
import com.example.bookchat.domain.model.OAuth2Provider

fun com.example.bookchat.data.network.model.OAuth2ProviderNetwork.toOAuth2Provider(): OAuth2Provider {
	return when (this) {
		com.example.bookchat.data.network.model.OAuth2ProviderNetwork.GOOGLE -> OAuth2Provider.GOOGLE
		com.example.bookchat.data.network.model.OAuth2ProviderNetwork.KAKAO -> OAuth2Provider.KAKAO
	}
}

fun OAuth2Provider.toOAuth2ProviderNetwork(): com.example.bookchat.data.network.model.OAuth2ProviderNetwork {
	return when (this) {
		OAuth2Provider.GOOGLE -> com.example.bookchat.data.network.model.OAuth2ProviderNetwork.GOOGLE
		OAuth2Provider.KAKAO -> com.example.bookchat.data.network.model.OAuth2ProviderNetwork.KAKAO
	}
}