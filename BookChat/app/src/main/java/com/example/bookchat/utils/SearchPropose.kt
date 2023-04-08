package com.example.bookchat.utils

import java.io.Serializable

sealed class SearchPurpose : Serializable {
    object DefaultSearch : SearchPurpose()
    object MakeChatRoom : SearchPurpose()
    object SearchChatRoom : SearchPurpose()
}