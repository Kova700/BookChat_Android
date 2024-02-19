package com.example.bookchat.data.response

import com.example.bookchat.data.Chat
import com.google.gson.annotations.SerializedName

data class RespondGetChat(
    @SerializedName("chatResponseList")
    val chatResponseList: List<Chat>,
    @SerializedName("cursorMeta")
    val cursorMeta: CursorMeta
)