package com.example.bookchat.data.response

data class CursorMeta(
    val sliceSize :Int,
    val contentSize :Int,
    val hasContent :Boolean,
    val hasNext :Boolean,
    val last :Boolean,
    val first :Boolean,
    val nextCursorId :Int //Int 인지 확인
)
