package com.example.bookchat.data.mapper

import com.example.bookchat.data.request.BookRequest
import com.example.bookchat.data.response.BookResponse
import com.example.bookchat.domain.model.Book

fun BookResponse.toBook(): Book {
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

fun List<BookResponse>.toBook() = this.map { it.toBook() }