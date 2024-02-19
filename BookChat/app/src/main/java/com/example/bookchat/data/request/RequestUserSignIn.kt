package com.example.bookchat.data.request

import com.example.bookchat.utils.OAuth2Provider
import com.google.gson.annotations.SerializedName

data class RequestUserSignIn(
    @SerializedName("fcmToken")
    val fcmToken: String, //FCM 토큰 객체로 수정
    @SerializedName("deviceToken")
    val deviceToken: String, //UUID로 수정
    @SerializedName("approveChangingDevice")
    val approveChangingDevice: Boolean, //처음엔 null, false로 보내고,
    @SerializedName("oauth2Provider")
    val oauth2Provider: OAuth2Provider
)

//200이 오면 정상 로그인
//다른기기에서 로그인 했었다면 409 conflict가 넘어옴,
//여기서 같은 요청을 approveChangingDevice를 true로 바꾸고(다른기기를 로그아웃하시겠습니까? 물어보고)
//보내면 해당 기기로 정상 로그인
//아니요 누르면 그냥 아예 안하면 되는