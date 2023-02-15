package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class BookSearchSortOption {
    @SerializedName("ACCURACY")
    ACCURACY,
    @SerializedName("LATEST")
    LATEST
}
