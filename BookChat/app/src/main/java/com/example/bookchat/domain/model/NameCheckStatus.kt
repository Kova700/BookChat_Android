package com.example.bookchat.domain.model

sealed class NameCheckStatus {
    object Default : NameCheckStatus()
    object IsShort : NameCheckStatus()
    object IsDuplicate : NameCheckStatus()
    object IsSpecialCharInText : NameCheckStatus()
    object IsPerfect : NameCheckStatus()
}