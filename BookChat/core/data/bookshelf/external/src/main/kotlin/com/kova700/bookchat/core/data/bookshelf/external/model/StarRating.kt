package com.kova700.bookchat.core.data.bookshelf.external.model

enum class StarRating(val value: Float) {
	ZERO(0F), HALF(0.5F),
	ONE(1F), ONE_HALF(1.5F),
	TWO(2F), TWO_HALF(2.5F),
	THREE(3F), THREE_HALF(3.5F),
	FOUR(4F), FOUR_HALF(4.5F),
	FIVE(5F)
}

fun Float.toStarRating(): StarRating = StarRating.entries.find { it.value == this }
	?: throw IllegalArgumentException("this value can't be converted to StarRating")