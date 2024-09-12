package com.kova700.bookchat.core.data.bookshelf.internal.mapper

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.network.bookchat.model.request.BookRequest

/** 외부 API로 가져온 도서를 서재에 해당 도서를 등록함과 동시에 서버 DB에 저장하기 위해 존재*/
fun Book.toBookRequest(): BookRequest {
	return BookRequest(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}