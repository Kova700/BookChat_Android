package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class OAuth2Provider(val provider: String){
    @SerializedName("GOOGLE")
    GOOGLE("google"),
    @SerializedName("KAKAO")
    KAKAO("kakao")
}
