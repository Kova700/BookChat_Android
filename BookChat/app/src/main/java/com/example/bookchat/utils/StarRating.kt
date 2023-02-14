package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class StarRating(val value :Float) {
    @SerializedName("ZERO") ZERO(0F), @SerializedName("HALF") HALF(0.5F),
    @SerializedName("ONE") ONE(1F), @SerializedName("ONE_HALF") ONE_HALF(1.5F),
    @SerializedName("TWO") TWO(2F), @SerializedName("TWO_HALF") TWO_HALF(2.5F),
    @SerializedName("THREE") THREE(3F), @SerializedName("THREE_HALF") THREE_HALF(3.5F),
    @SerializedName("FOUR") FOUR(4F), @SerializedName("FOUR_HALF") FOUR_HALF(4.5F),
    @SerializedName("FIVE") FIVE(5F)
}

fun Float.toStarRating() :StarRating{
    return when(this){
        0F -> { StarRating.ZERO }; 0.5F -> { StarRating.HALF}
        1.0F -> { StarRating.ONE }; 1.5F -> { StarRating.ONE_HALF }
        2.0F -> { StarRating.TWO }; 2.5F -> { StarRating.TWO_HALF }
        3.0F -> { StarRating.THREE }; 3.5F -> { StarRating.THREE_HALF }
        4.0F -> { StarRating.FOUR }; 4.5F -> { StarRating.FOUR_HALF }
        5.0F -> { StarRating.FIVE }; else -> { throw Exception("this value can't be converted to StarRating") }
    }
}