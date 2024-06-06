package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.BookStateInBookShelf
import com.example.bookchat.domain.model.SearchSortOption
import com.example.bookchat.domain.model.StarRating
import kotlinx.coroutines.flow.Flow

interface BookShelfRepository {
	suspend fun registerBookShelfBook(
		book: Book,
		bookShelfState: BookShelfState,
		starRating: StarRating? = null,
	)

	suspend fun deleteBookShelfBook(bookShelfItemId: Long, bookShelfState: BookShelfState)
	suspend fun changeBookShelfBookStatus(
		bookShelfItemId: Long,
		newBookShelfItem: BookShelfItem,
	)

	suspend fun checkAlreadyInBookShelf(book: Book): BookStateInBookShelf

	fun getBookShelfFlow(bookShelfState: BookShelfState): Flow<List<BookShelfItem>>
	suspend fun getBookShelfItems(
		bookShelfState: BookShelfState,
		size: Int = BOOKSHELF_ITEM_LOAD_SIZE,
		sort: SearchSortOption = SearchSortOption.UPDATED_AT_DESC,
	)

	suspend fun getBookShelfItem(
		bookShelfId: Long,
		bookShelfState: BookShelfState,
	): BookShelfItem

	fun getCachedBookShelfItem(bookShelfItemId: Long): BookShelfItem
	fun getBookShelfTotalItemCountFlow(bookShelfState: BookShelfState): Flow<Int>

	companion object {
		private const val BOOKSHELF_ITEM_LOAD_SIZE = 20
		const val BOOKSHELF_ITEM_FIRST_PAGE = 0L
	}

}