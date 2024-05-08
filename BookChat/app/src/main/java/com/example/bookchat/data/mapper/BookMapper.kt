package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.request.BookRequest
import com.example.bookchat.data.network.model.response.BookSearchResponse
import com.example.bookchat.domain.model.Book

fun com.example.bookchat.data.network.model.response.BookSearchResponse.toBook(): Book {
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
fun Book.toBookRequest(): com.example.bookchat.data.network.model.request.BookRequest {
	return com.example.bookchat.data.network.model.request.BookRequest(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

fun List<com.example.bookchat.data.network.model.response.BookSearchResponse>.toBook() = this.map { it.toBook() }