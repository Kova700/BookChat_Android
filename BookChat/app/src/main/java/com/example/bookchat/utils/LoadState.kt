package com.example.bookchat.utils

sealed class LoadState {
    object Default :LoadState()
    object Loading :LoadState()
    object Result :LoadState()
}