package com.example.bookchat.domain.model

enum class StarRating(val value: Float) {
	ZERO(0F), HALF(0.5F),
	ONE(1F), ONE_HALF(1.5F),
	TWO(2F), TWO_HALF(2.5F),
	THREE(3F), THREE_HALF(3.5F),
	FOUR(4F), FOUR_HALF(4.5F),
	FIVE(5F)
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