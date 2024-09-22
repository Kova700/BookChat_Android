package com.kova700.bookchat.core.network.bookchat.bookshelf.model.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.both.BookShelfStateNetwork

fun BookShelfState.toNetwork(): BookShelfStateNetwork {
	return BookShelfStateNetwork.valueOf(name)
}

fun BookShelfStateNetwork.toDomain(): BookShelfState {
	return BookShelfState.valueOf(name)
}