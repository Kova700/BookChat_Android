package com.example.bookchat.data

import com.example.bookchat.utils.OAuth2Provider
import com.example.bookchat.utils.ReadingTaste
import okhttp3.MultipartBody
import java.io.Serializable

data class UserSignUpDto(
    val oauth2Provider : OAuth2Provider? = null,
    var nickname: String = "",
    var readingTastes : List<ReadingTaste> = arrayListOf(),
    var userProfileImage : MultipartBody.Part? = null
) : Serializable
