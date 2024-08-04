package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toBookRequest
import com.example.bookchat.data.mapper.toDomain
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestChangeBookStatus
import com.example.bookchat.data.network.model.request.RequestRegisterBookShelfBook
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.BookStateInBookShelf
import com.example.bookchat.domain.model.SearchSortOption
import com.example.bookchat.domain.model.StarRating
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.domain.repository.BookShelfRepository.Companion.BOOKSHELF_ITEM_FIRST_PAGE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

//TODO : DB적용 후, ROOM에서 Flow로 가저오도록 수정
class BookShelfRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
) : BookShelfRepository {
	private val mapBookShelfItems =
		MutableStateFlow<Map<Long, BookShelfItem>>(mapOf()) //(ItemId, Item)

	//현재 가지고 있는 데이터를 기준으로 totalItemCount를 세는 중,
	//데이터 삭제, 상태 이동 시, 해당 숫자 변화는 그대로 반영할 수 있으나,
	//전체 데이터 개수와 차이가 생김(데이터를 전부 로드하지 않은 상황이라면, 서버와 클라이언트의 개수 차이가 생김)

	//그렇다고 서버의 데이터를 우선적으로 적용하기 위해서는,
	//데이터를 페이징할때만 데이터 갱신이 발생하고, 삭제 상태 이동 시, 따로 숫자를 카운팅 해주어야함
	//

	//그리고 로컬 데이터 개수만 셀꺼라면 굳이 여기 말고 Ui레이어로 올려도 됨
//	private val totalItemCount =
//		mapBookShelfItems.map { itemsMap ->
//			itemsMap.values.groupBy { it.state }
//				.mapValues { (bookShelfState, items) -> items.size }
//		}

	private val totalItemCount = MutableStateFlow<Map<BookShelfState, Int>>(mapOf())

	private val currentPages: MutableMap<BookShelfState, Long> = mutableMapOf()
	private var isEndPages: MutableMap<BookShelfState, Boolean> = mutableMapOf()

	override fun getBookShelfFlow(bookShelfState: BookShelfState): Flow<List<BookShelfItem>> {
		return mapBookShelfItems.map { items ->
			items.values.toList()
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

		val response = bookChatApi.getBookShelfItems(
			size = size,
			page = currentPages[bookShelfState] ?: BOOKSHELF_ITEM_FIRST_PAGE,
			bookShelfState = bookShelfState.name,
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

	//TODO : 조회 반환값에 서재의 어떤 상태인지 추가되면 매우 좋을듯
	// (다중 조회랑 단건 조회랑 스펙이 다름) (가져온놈이 어떤 상태인지는 알아야할 듯)
	// 수정되면 getCachedBookShelfItem이랑 통합
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

	private suspend fun getOnlineBookShelfItem(
		bookShelfId: Long,
		bookShelfState: BookShelfState,
	): BookShelfItem {
		val bookShelfItem = bookChatApi.getBookShelfItem(bookShelfId).toDomain(bookShelfState)
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

		val response = bookChatApi.registerBookShelfBook(requestRegisterBookShelfBook)

		val createdItemID = response.headers()["Location"]
			?.split("/")?.last()?.toLong()
			?: throw Exception("bookShelfId does not exist in Http header.")

		getOnlineBookShelfItem(
			bookShelfId = createdItemID,
			bookShelfState = bookShelfState
		)
		totalItemCount.update { it + (bookShelfState to (it[bookShelfState]?.plus(1) ?: 1)) }
	}

	override suspend fun deleteBookShelfBook(bookShelfItemId: Long) {
		bookChatApi.deleteBookShelfBook(bookShelfItemId)

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

		bookChatApi.changeBookShelfBookStatus(bookShelfItemId, request)

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
		val response = bookChatApi.checkAlreadyInBookShelf(book.isbn, book.publishAt)
		if (response.isSuccessful) return response.body()?.toDomain()
		when (response.code()) {
			404 -> return null
			else -> throw Exception("Unexpected response code: ${response.code()}")
		}
	}

	override fun clear() {
		mapBookShelfItems.update { emptyMap() }
		currentPages.clear()
		isEndPages.clear()
	}
}