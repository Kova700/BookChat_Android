package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.BookSearchSortOptionNetWork
import com.example.bookchat.domain.model.BookSearchSortOption

fun BookSearchSortOption.toNetWork(): BookSearchSortOptionNetWork {
	return when (this) {
		BookSearchSortOption.ACCURACY -> BookSearchSortOptionNetWork.ACCURACY
		BookSearchSortOption.LATEST -> BookSearchSortOptionNetWork.LATEST
	}
}