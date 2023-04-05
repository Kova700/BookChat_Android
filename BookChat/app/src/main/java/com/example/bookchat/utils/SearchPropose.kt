package com.example.bookchat.utils

import java.io.Serializable

sealed class SearchPurpose : Serializable {
    object Search : SearchPurpose()
    object MakeChatRoom : SearchPurpose()
}
