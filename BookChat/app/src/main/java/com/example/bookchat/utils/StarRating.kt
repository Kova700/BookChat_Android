package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class StarRating {
    @SerializedName("ZERO") ZERO, @SerializedName("HALF") HALF,
    @SerializedName("ONE") ONE, @SerializedName("ONE_HALF") ONE_HALF,
    @SerializedName("TWO") TWO, @SerializedName("TWO_HALF") TWO_HALF,
    @SerializedName("THREE") THREE, @SerializedName("THREE_HALF") THREE_HALF,
    @SerializedName("FOUR") FOUR, @SerializedName("FOUR_HALF") FOUR_HALF,
    @SerializedName("FIVE") FIVE
}