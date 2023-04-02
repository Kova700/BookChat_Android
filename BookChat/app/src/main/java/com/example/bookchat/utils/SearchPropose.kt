package com.example.bookchat.utils

sealed class SearchPurpose{
    object Search :SearchPurpose()
    object MakeChatRoom :SearchPurpose()
}
