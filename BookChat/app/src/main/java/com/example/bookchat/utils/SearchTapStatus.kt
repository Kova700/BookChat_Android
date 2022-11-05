package com.example.bookchat.utils

sealed class SearchTapStatus{
    object Default :SearchTapStatus() //추천검색어
    object History :SearchTapStatus() //최근 검색어
    object Searching :SearchTapStatus() //관련 도서명
    object Loading :SearchTapStatus() //isLoading
    object Result :SearchTapStatus() //ShowResult
    object Detail :SearchTapStatus() //ShowResult
}
