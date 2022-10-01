package com.example.bookchat.data

import com.example.bookchat.utils.IdTokenProvider

data class IdToken(
    val token : String,
    var idTokenProvider: IdTokenProvider
)
