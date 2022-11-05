package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

//포스트맨 넘어가는거 확인
enum class BookSearchSortOption {
    @SerializedName("ACCURACY") ACCURACY,
    @SerializedName("LATEST") LATEST
}
