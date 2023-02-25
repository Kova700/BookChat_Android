package com.example.bookchat.utils

object RefreshManager {
    var bookShelfRefreshList = mutableListOf<BookShelfRefreshFlag>()

    fun hasWishBookShelfNewData(): Boolean =
        bookShelfRefreshList.contains(BookShelfRefreshFlag.Wish)

    fun hasReadingBookShelfNewData(): Boolean =
        bookShelfRefreshList.contains(BookShelfRefreshFlag.Reading)

    fun hasCompleteBookShelfNewData(): Boolean =
        bookShelfRefreshList.contains(BookShelfRefreshFlag.Complete)

    fun popRefreshWishFlag() {
        bookShelfRefreshList =
            bookShelfRefreshList.filter { it != BookShelfRefreshFlag.Wish }.toMutableList()
    }

    fun popRefreshReadingFlag() {
        bookShelfRefreshList =
            bookShelfRefreshList.filter { it != BookShelfRefreshFlag.Reading }.toMutableList()
    }

    fun popRefreshCompleteFlag() {
        bookShelfRefreshList =
            bookShelfRefreshList.filter { it != BookShelfRefreshFlag.Complete }.toMutableList()
    }

    fun addBookShelfRefreshFlag(flag: BookShelfRefreshFlag) {
        if (bookShelfRefreshList.contains(flag)) return
        bookShelfRefreshList.add(flag)
    }

    sealed class BookShelfRefreshFlag {
        object Wish : BookShelfRefreshFlag()
        object Reading : BookShelfRefreshFlag()
        object Complete : BookShelfRefreshFlag()
    }
}