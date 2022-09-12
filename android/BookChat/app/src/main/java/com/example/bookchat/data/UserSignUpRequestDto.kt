package com.example.bookchat.data

import com.example.bookchat.utils.ReadingTaste
import okhttp3.MultipartBody
import java.io.Serializable


data class UserSignUpRequestDto(
    val nickname: String = "",
    val userEmail: String = "",
    val oauth2Provider :String = "",
    val defaultProfileImageType : Int,
    val userProfileImage : MultipartBody.Part,
    val subImgNum : Int, //등록 이미지 없을 시 기본이미지 노출하게 (기본 이미지 번호)
    val readingTastes : List<ReadingTaste> = arrayListOf()
) : Serializable
