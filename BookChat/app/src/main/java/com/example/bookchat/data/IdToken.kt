package com.example.bookchat.data

import com.example.bookchat.utils.OAuth2Provider

data class IdToken(
    val token : String,
    var oAuth2Provider: OAuth2Provider
)

