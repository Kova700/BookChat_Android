package com.example.bookchat.data.request

import com.example.bookchat.utils.OAuth2Provider
import com.example.bookchat.utils.ReadingTaste
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class RequestUserSignUp(
    @SerializedName("oauth2Provider")
    val oauth2Provider : OAuth2Provider,
    @SerializedName("nickname")
    var nickname: String,
    @SerializedName("readingTastes")
    var readingTastes : List<ReadingTaste>,
    @SerializedName("defaultProfileImageType")
    val defaultProfileImageType : Int = Random().nextInt(5) + 1,
) : Serializable
