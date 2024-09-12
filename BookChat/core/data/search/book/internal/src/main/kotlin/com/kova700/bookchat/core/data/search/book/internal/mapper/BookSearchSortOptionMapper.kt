package com.kova700.bookchat.core.data.search.book.internal.mapper

import com.kova700.bookchat.core.network.bookchat.model.both.BookSearchSortOptionNetWork
import com.kova700.bookchat.core.data.search.book.external.model.BookSearchSortOption

fun BookSearchSortOption.toNetWork(): BookSearchSortOptionNetWork {
	return BookSearchSortOptionNetWork.valueOf(name)
}