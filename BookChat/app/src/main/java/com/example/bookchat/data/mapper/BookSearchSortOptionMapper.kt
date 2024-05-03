package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.BookSearchSortOptionNetWork
import com.example.bookchat.domain.model.BookSearchSortOption

fun BookSearchSortOption.toNetWork(): com.example.bookchat.data.network.model.BookSearchSortOptionNetWork {
	return when (this) {
		BookSearchSortOption.ACCURACY -> com.example.bookchat.data.network.model.BookSearchSortOptionNetWork.ACCURACY
		BookSearchSortOption.LATEST -> com.example.bookchat.data.network.model.BookSearchSortOptionNetWork.LATEST
	}
}