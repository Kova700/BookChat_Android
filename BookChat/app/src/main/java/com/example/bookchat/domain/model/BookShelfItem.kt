package com.example.bookchat.domain.model

//TODO : dispatchTime 같은 정렬 요소가 필요함
data class BookShelfItem(
	val bookShelfId: Long,
	val book: Book,
	val pages: Int,
	val star: StarRating? = null,
	val state: BookShelfState,
) {
	companion object {
		val DEFAULT = BookShelfItem(
			bookShelfId = 0L,
			book = Book.DEFAULT,
			pages = 0,
			state = BookShelfState.WISH,
		)
	}
}