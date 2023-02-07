package com.example.bookchat.data.request

import com.example.bookchat.utils.OAuth2Provider
import com.google.gson.annotations.SerializedName

data class RequestUserSignIn(
    @SerializedName("oauth2Provider")
    val oauth2Provider :OAuth2Provider
)
