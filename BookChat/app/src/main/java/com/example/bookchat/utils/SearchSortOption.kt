package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class SearchSortOption(option :String) {
    @SerializedName("id,DESC") DESC("id,DESC"), //최신순
    @SerializedName("id,ASC") ASC("id,ASC") //등록순
}