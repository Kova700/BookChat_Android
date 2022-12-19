package com.example.bookchat.request

import com.example.bookchat.utils.AgonyFolderHexColor
import com.google.gson.annotations.SerializedName

//RequestMakeAgonyFolder와 내용물이 같음으로 합칠 수 있을 것 같음
data class RequestReviseAgony(
    @SerializedName("agonyTitle")
    val agonyTitle :String,
    @SerializedName("agonyColor")
    val agonyColor : AgonyFolderHexColor
)
