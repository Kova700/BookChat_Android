package com.example.bookchat.request

import com.example.bookchat.utils.OAuth2Provider
import com.example.bookchat.utils.ReadingTaste
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RequestUserSignUp(
    @SerializedName("oauth2Provider")
    val oauth2Provider : OAuth2Provider,
    @SerializedName("nickname")
    var nickname: String,
    @SerializedName("defaultProfileImageType")
    val defaultProfileImageType : Int?,
    @SerializedName("readingTastes")
    var readingTastes : List<ReadingTaste>,
) : Serializable
