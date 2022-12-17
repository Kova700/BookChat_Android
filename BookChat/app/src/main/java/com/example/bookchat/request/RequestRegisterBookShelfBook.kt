package com.example.bookchat.request

import com.example.bookchat.data.Book
import com.example.bookchat.utils.ReadingStatus
import com.google.gson.annotations.SerializedName

class RequestRegisterBookShelfBook(
    book: Book,
    @SerializedName("readingStatus")
    private val readingStatus :ReadingStatus,
    @SerializedName("star")
    private val star : String? = null
){
    //일단 이렇게 구현해놓고, 백엔드에서 Book entity
    // dateTime -> publishAt, author -> authors로 수정되면 그냥
    // @SerializedName("bookRequest")  book: Book, 로 사용할 예정

    private val bookRequest = BookRequest(
        isbn = book.isbn,
        title = book.title,
        authors = book.authors,
        publisher = book.publisher,
        bookCoverImageUrl = book.bookCoverImageUrl,
        publishAt = book.datetime
    )
}
