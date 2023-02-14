package com.example.bookchat.data.response

import com.example.bookchat.data.AgonyRecord
import com.google.gson.annotations.SerializedName

data class ResponseGetAgonyRecord(
    @SerializedName("agonyRecordResponseList")
    val agonyRecordResponseList :List<AgonyRecord>,
    @SerializedName("cursorMeta")
    val cursorMeta : CursorMeta
)
