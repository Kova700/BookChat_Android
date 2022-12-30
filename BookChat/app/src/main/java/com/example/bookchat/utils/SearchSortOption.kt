package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class SearchSortOption {
    @SerializedName("id,DESC")
    DESC,  //최신순
    @SerializedName("id,ASC")
    ASC    //등록순
}