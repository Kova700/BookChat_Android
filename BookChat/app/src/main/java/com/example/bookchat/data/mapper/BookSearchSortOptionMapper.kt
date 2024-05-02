package com.example.bookchat.data.mapper

import com.example.bookchat.data.model.BookSearchSortOptionNetWork
import com.example.bookchat.domain.model.BookSearchSortOption

fun BookSearchSortOption.toNetWork(): BookSearchSortOptionNetWork {
	return when (this) {
		BookSearchSortOption.ACCURACY -> BookSearchSortOptionNetWork.ACCURACY
		BookSearchSortOption.LATEST -> BookSearchSortOptionNetWork.LATEST
	}
}