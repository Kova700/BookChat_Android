package com.example.bookchat.data


data class tempUser(
    val nickname: String,
    val userEmail: String,
    val userProfileImageUri: String, //이거 수정해야할듯..? 기본값이랑 사진파일값 반영가능하게
    val readingTaste : ArrayList<String>
)
