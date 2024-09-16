package com.kova700.bookchat.core.network.bookchat.common.model

import com.google.gson.annotations.SerializedName

data class CursorMeta(
    @SerializedName("sliceSize")
    val sliceSize: Int,
    @SerializedName("contentSize")
    val contentSize: Int,
    @SerializedName("hasContent")
    val hasContent: Boolean,
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("last")
    val last: Boolean,
    @SerializedName("first")
    val first: Boolean,
    @SerializedName("nextCursorId")
    val nextCursorId: Long?,
)
