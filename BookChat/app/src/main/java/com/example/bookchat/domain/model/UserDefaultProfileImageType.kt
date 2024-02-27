package com.example.bookchat.domain.model

import com.google.gson.annotations.SerializedName

enum class UserDefaultProfileImageType(val num :Int) {
    @SerializedName("1") ONE(1),
    @SerializedName("2") TWO(2),
    @SerializedName("3") THREE(3),
    @SerializedName("4") FOUR(4),
    @SerializedName("5") FIVE(5),
}