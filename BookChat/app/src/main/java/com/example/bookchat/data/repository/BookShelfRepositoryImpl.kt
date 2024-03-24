package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.mapper.toBookRequest
import com.example.bookchat.data.mapper.toBookShelfItem
import com.example.bookchat.data.mapper.toBookStateInBookShelf
import com.example.bookchat.data.request.RequestChangeBookStatus
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.BookStateInBookShelf
import com.example.bookchat.domain.model.StarRating
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.domain.repository.BookShelfRepository.Companion.BOOKSHELF_ITEM_FIRST_PAGE
import com.example.bookchat.utils.SearchSortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

//TODO : DB적용 후, ROOM에서 Flow로 가저오도록 수정
class BookShelfRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : BookShelfRepository {
	private val mapBookShelfItems =
		MutableStateFlow<Map<Long, BookShelfItem>>(mapOf()) //(ItemId, Item)

	//TODO : totalItemCount 추후 수정 (불필요한 로직 Too much)
	private val totalItemCount =
		MutableStateFlow<Map<BookShelfState, Int>>(mapOf()) //(bookshelfState, totalCount)

	private val currentPages: MutableMap<BookShelfState, Long> = mutableMapOf()
	private var isEndPages: MutableMap<BookShelfState, Boolean> = mutableMapOf()

	override fun getBookShelfFlow(bookShelfState: BookShelfState): Flow<List<BookShelfItem>> {
		return mapBookShelfItems.map { items ->
			items.values.toList().filter {
				it.state == bookShelfState
				//TODO :dispatchTime 같은 정렬 요소가 필요함
			}.sortedWith(compareBy { bookshelfItem -> -bookshelfItem.bookShelfId })
		}.distinctUntilChanged().filterNotNull()
	}

	override suspend fun getBookShelfItems(
		bookShelfState: BookShelfState,
		size: Int,
		sort: SearchSortOption
	) {
		if (isEndPages[bookShelfState] == true) return

		val response = bookChatApi.getBookShelfItems(
			size = size,
			page = currentPages[bookShelfState] ?: BOOKSHELF_ITEM_FIRST_PAGE,
			bookShelfState = bookShelfState.name,
			sort = sort
		)

		isEndPages[bookShelfState] = response.pageMeta.last
		val existingPage = currentPages[bookShelfState] ?: BOOKSHELF_ITEM_FIRST_PAGE
		currentPages[bookShelfState] = existingPage + 1

		totalItemCount.update {
			totalItemCount.value + (bookShelfState to response.pageMeta.totalElements)
		}

		mapBookShelfItems.update {
			mapBookShelfItems.value + response.contents.map { it.toBookShelfItem(bookShelfState) }
				.associateBy { it.bookShelfId }
		}
	}

	override fun getBookShelfTotalItemCountFlow(bookShelfState: BookShelfState): Flow<Int> {
		return totalItemCount.map { it[bookShelfState] ?: 0 }
	}

	override fun getCachedBookShelfItem(bookShelfItemId: Long): BookShelfItem? {
		return mapBookShelfItems.value[bookShelfItemId]
	}

	//TOOD : POST 요청시 생성 후 결과 body로 받아오게 수정
	override suspend fun registerBookShelfBook(
		book: Book,
		bookShelfState: BookShelfState,
		starRating: StarRating?
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(
			bookRequest = book.toBookRequest(),
			bookShelfState = bookShelfState,
			star = starRating
		)
		bookChatApi.registerBookShelfBook(requestRegisterBookShelfBook)
		val newTotalCount = (totalItemCount.value[bookShelfState] ?: 0) + 1
		totalItemCount.value = totalItemCount.value + (bookShelfState to newTotalCount)
	}

	override suspend fun deleteBookShelfBook(
		bookShelfItemId: Long,
		bookShelfState: BookShelfState
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		bookChatApi.deleteBookShelfBook(bookShelfItemId)

		mapBookShelfItems.update { mapBookShelfItems.value - bookShelfItemId }
		val newTotalCount = maxOf((totalItemCount.value[bookShelfState] ?: 0) - 1, 0)
		totalItemCount.value = totalItemCount.value + (bookShelfState to newTotalCount)
	}

	override suspend fun changeBookShelfBookStatus(
		bookShelfItemId: Long,
		newBookShelfItem: BookShelfItem,
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val request = RequestChangeBookStatus(
			bookShelfState = newBookShelfItem.state,
			star = newBookShelfItem.star,
			pages = newBookShelfItem.pages
		)
		bookChatApi.changeBookShelfBookStatus(bookShelfItemId, request)

		val previousState = mapBookShelfItems.value[bookShelfItemId]?.state ?: return
		mapBookShelfItems.update {
			mapBookShelfItems.value + (bookShelfItemId to newBookShelfItem)
		}

		val newTotalCount = (totalItemCount.value[newBookShelfItem.state] ?: 0) + 1
		totalItemCount.value = totalItemCount.value + (newBookShelfItem.state to newTotalCount)

		val previousStateNewTotalCount = maxOf((totalItemCount.value[previousState] ?: 0) - 1, 0)
		totalItemCount.value = totalItemCount.value + (previousState to previousStateNewTotalCount)
	}

	override suspend fun checkAlreadyInBookShelf(book: Book): BookStateInBookShelf {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		return bookChatApi.checkAlreadyInBookShelf(book.isbn, book.publishAt)
			.toBookStateInBookShelf()
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}
}