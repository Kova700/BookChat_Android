package com.kova700.bookchat.core.data.search.book.internal.mapper

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.network.bookchat.model.request.BookRequest
import com.kova700.bookchat.core.network.bookchat.model.response.BookSearchResponse

fun BookSearchResponse.toBook(): Book {
	return Book(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

//request시 해당 타입이 왜 필요한 가 의문점이 들긴함
//ID가 있다면 ID만 보내면 되지 않을까?
//카카오 도서 정보를 서버에 저장하기 위함인가?
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

fun List<BookSearchResponse>.toBook() = this.map { it.toBook() }