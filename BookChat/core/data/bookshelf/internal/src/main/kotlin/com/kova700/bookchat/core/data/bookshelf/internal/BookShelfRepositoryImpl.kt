package com.kova700.bookchat.core.data.bookshelf.internal

import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository.Companion.BOOKSHELF_ITEM_FIRST_PAGE
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.BookStateInBookShelf
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import com.kova700.bookchat.core.data.common.model.SearchSortOption
import com.kova700.bookchat.core.data.common.model.network.BookChatApiResult
import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.network.bookchat.bookshelf.BookshelfApi
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.mapper.toDomain
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.mapper.toNetwork
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.request.RequestChangeBookStatus
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.request.RequestRegisterBookShelfBook
import com.kova700.bookchat.core.network.bookchat.common.mapper.toBookRequest
import com.kova700.bookchat.core.network.bookchat.common.mapper.toNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

//TODO : [Version 2] DB적용 후, ROOM에서 Flow로 가저오도록 수정
class BookShelfRepositoryImpl @Inject constructor(
	private val bookshelfApi: BookshelfApi,
) : BookShelfRepository {
	private val mapBookShelfItems =
		MutableStateFlow<Map<Long, BookShelfItem>>(mapOf()) //(ItemId, Item)

	/** 서버로부터 모든 데이터를 한 번에 가져오지 않음으로, 검색 결과 MetaData로부터 갱신하기 위해 totalItemCount를 따로 분리 */
	private val totalItemCount = MutableStateFlow<Map<BookShelfState, Int>>(mapOf())

	private val currentPages: MutableMap<BookShelfState, Long> = mutableMapOf()
	private var isEndPages: MutableMap<BookShelfState, Boolean> = mutableMapOf()

	override fun getBookShelfFlow(bookShelfState: BookShelfState): Flow<List<BookShelfItem>> {
		return mapBookShelfItems.map { items ->
			items.values
				.filter { it.state == bookShelfState }
				//ORDER BY last_updated_at DESC, bookshelf_id DESC
				.sortedWith(compareBy(
					{ bookshelfItem -> bookshelfItem.lastUpdatedAt.time.unaryMinus() },
					{ bookshelfItem -> bookshelfItem.bookShelfId.unaryMinus() }
				))
		}.distinctUntilChanged().filterNotNull()
	}

	private fun setBookShelfItems(newBookShelfItems: Map<Long, BookShelfItem>) {
		mapBookShelfItems.update { newBookShelfItems }
	}

	override suspend fun getBookShelfItems(
		bookShelfState: BookShelfState,
		size: Int,
		sort: SearchSortOption,
	) {
		if (isEndPages[bookShelfState] == true) return

		val response = bookshelfApi.getBookShelfItems(
			size = size,
			page = currentPages[bookShelfState] ?: BOOKSHELF_ITEM_FIRST_PAGE,
			bookShelfState = bookShelfState.toNetwork(),
			sort = sort.toNetwork()
		)

		isEndPages[bookShelfState] = response.pageMeta.last
		val existingPage = currentPages[bookShelfState] ?: BOOKSHELF_ITEM_FIRST_PAGE
		currentPages[bookShelfState] = existingPage + 1

		setBookShelfItems(
			mapBookShelfItems.value + response.contents
				.map { it.toDomain(bookShelfState) }
				.associateBy { it.bookShelfId }
		)
		totalItemCount.update { it + (bookShelfState to response.pageMeta.totalElements) }
	}

	override suspend fun getBookShelfItem(
		bookShelfId: Long,
		bookShelfState: BookShelfState,
	): BookShelfItem {
		return mapBookShelfItems.value[bookShelfId]
			?: getOnlineBookShelfItem(
				bookShelfId = bookShelfId,
				bookShelfState = bookShelfState
			)
	}

	//TODO : [NotUrgent] 단건 조회 반환 값에 서재의 어떤 상태인지 추가되면 매우 좋을듯
	// (다중 조회랑 단건 조회랑 스펙이 다름) (가져온놈이 어떤 상태인지는 알아야할 듯)
	// 수정되면 getCachedBookShelfItem이랑 통합
	private suspend fun getOnlineBookShelfItem(
		bookShelfId: Long,
		bookShelfState: BookShelfState,
	): BookShelfItem {
		val bookShelfItem = bookshelfApi.getBookShelfItem(bookShelfId).toDomain(bookShelfState)
		setBookShelfItems(mapBookShelfItems.value + (bookShelfItem.bookShelfId to bookShelfItem))
		return bookShelfItem
	}

	override fun getBookShelfTotalItemCountFlow(bookShelfState: BookShelfState): Flow<Int> {
		return totalItemCount.map { it[bookShelfState] ?: 0 }
	}

	override fun getCachedBookShelfItem(bookShelfItemId: Long): BookShelfItem {
		return mapBookShelfItems.value[bookShelfItemId]!!
	}

	override suspend fun registerBookShelfBook(
		book: Book,
		bookShelfState: BookShelfState,
		starRating: StarRating?,
	) {
		val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(
			bookRequest = book.toBookRequest(),
			bookShelfState = bookShelfState,
			star = starRating
		)

		val response = bookshelfApi.registerBookShelfBook(requestRegisterBookShelfBook)

		val createdItemID = response.locationHeader
			?: throw Exception("bookShelfId does not exist in Http header.")

		getOnlineBookShelfItem(
			bookShelfId = createdItemID,
			bookShelfState = bookShelfState
		)
		totalItemCount.update { it + (bookShelfState to (it[bookShelfState]?.plus(1) ?: 1)) }
	}

	override suspend fun deleteBookShelfBook(bookShelfItemId: Long) {
		bookshelfApi.deleteBookShelfBook(bookShelfItemId)

		val bookShelfState = mapBookShelfItems.value[bookShelfItemId]?.state ?: return
		totalItemCount.update {
			it + (bookShelfState to maxOf(0, ((it[bookShelfState] ?: 0) - 1)))
		}
		setBookShelfItems(mapBookShelfItems.value - bookShelfItemId)
	}

	override suspend fun changeBookShelfBookStatus(
		bookShelfItemId: Long,
		newBookShelfItem: BookShelfItem,
	) {
		val request = RequestChangeBookStatus(
			bookShelfState = newBookShelfItem.state,
			star = newBookShelfItem.star,
			pages = newBookShelfItem.pages
		)

		bookshelfApi.changeBookShelfBookStatus(bookShelfItemId, request)

		val previousState = mapBookShelfItems.value[bookShelfItemId]?.state ?: return
		val newState = newBookShelfItem.state

		/** lastUpdate 시간 때문에 서버로부터 다시 받아옴 */
		getOnlineBookShelfItem(
			bookShelfId = bookShelfItemId,
			bookShelfState = newBookShelfItem.state
		)

		totalItemCount.update {
			it + (previousState to maxOf(0, ((it[previousState] ?: 0) - 1))) +
							(newState to (it[newState]?.plus(1) ?: 1))
		}
	}

	override suspend fun checkAlreadyInBookShelf(book: Book): BookStateInBookShelf? {
		val response = bookshelfApi.checkAlreadyInBookShelf(book.isbn, book.publishAt)
		return when (response) {
			is BookChatApiResult.Success -> response.data.toDomain()
			is BookChatApiResult.Failure -> {
				when (response.code) {
					404 -> null
					else -> throw Exception("Unexpected response code: ${response.code}")
				}
			}
		}
	}

	override fun clear() {
		mapBookShelfItems.update { emptyMap() }
		currentPages.clear()
		isEndPages.clear()
	}
}