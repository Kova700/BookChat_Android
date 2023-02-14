package com.example.bookchat.data.response

import com.example.bookchat.data.Agony
import com.google.gson.annotations.SerializedName

data class ResponseGetAgony(
    @SerializedName("agonyResponseList")
    val agonyResponseList :List<Agony>,
    @SerializedName("cursorMeta")
    val cursorMeta : CursorMeta
)
