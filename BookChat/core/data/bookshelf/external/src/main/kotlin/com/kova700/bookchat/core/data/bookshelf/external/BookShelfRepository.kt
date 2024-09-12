package com.kova700.bookchat.core.data.bookshelf.external

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.BookStateInBookShelf
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.util.model.SearchSortOption
import kotlinx.coroutines.flow.Flow

interface BookShelfRepository {
	suspend fun registerBookShelfBook(
		book: Book,
		bookShelfState: BookShelfState,
		starRating: StarRating? = null,
	)

	suspend fun deleteBookShelfBook(bookShelfItemId: Long)

	suspend fun changeBookShelfBookStatus(
		bookShelfItemId: Long,
		newBookShelfItem: BookShelfItem,
	)

	suspend fun checkAlreadyInBookShelf(book: Book): BookStateInBookShelf?

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

	fun clear()

	companion object {
		private const val BOOKSHELF_ITEM_LOAD_SIZE = 20
		const val BOOKSHELF_ITEM_FIRST_PAGE = 0L
	}
}