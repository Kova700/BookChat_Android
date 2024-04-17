package com.example.bookchat.data.mapper

import com.example.bookchat.data.model.OAuth2ProviderNetwork
import com.example.bookchat.domain.model.OAuth2Provider

fun OAuth2ProviderNetwork.toOAuth2Provider(): OAuth2Provider {
	return when (this) {
		OAuth2ProviderNetwork.GOOGLE -> OAuth2Provider.GOOGLE
		OAuth2ProviderNetwork.KAKAO -> OAuth2Provider.KAKAO
	}
}

fun OAuth2Provider.toOAuth2ProviderNetwork(): OAuth2ProviderNetwork {
	return when (this) {
		OAuth2Provider.GOOGLE -> OAuth2ProviderNetwork.GOOGLE
		OAuth2Provider.KAKAO -> OAuth2ProviderNetwork.KAKAO
	}
}