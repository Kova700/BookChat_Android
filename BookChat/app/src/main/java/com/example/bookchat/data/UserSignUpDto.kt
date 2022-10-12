package com.example.bookchat.data

import com.example.bookchat.utils.ReadingTaste
import okhttp3.MultipartBody
import java.io.Serializable


data class UserSignUpDto(
    val nickname: String = "",
    val defaultProfileImageType : Int, //등록 이미지 없을 시 기본이미지 노출하게 (기본 이미지 번호)
    var userProfileImage : MultipartBody.Part?,
    var readingTastes : List<ReadingTaste> = arrayListOf()
) : Serializable
