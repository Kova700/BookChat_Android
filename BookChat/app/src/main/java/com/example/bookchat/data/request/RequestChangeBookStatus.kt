package com.example.bookchat.data.request

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.StarRating
import com.google.gson.annotations.SerializedName

data class RequestChangeBookStatus(
	@SerializedName("readingStatus")
    private val bookShelfState : BookShelfState,
	@SerializedName("star")
    private val star : StarRating? = null,
	@SerializedName("pages")
    private val pages :Int? = null
)
