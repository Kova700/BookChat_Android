package com.example.bookchat.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.data.paging.CompleteBookTapPagingSource
import com.example.bookchat.data.paging.ReadingBookTapPagingSource
import com.example.bookchat.data.paging.WishBookTapPagingSource
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.utils.ReadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class BookShelfViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository
) : ViewModel() {
	private val _eventFlow = MutableSharedFlow<BookShelfEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	val wishBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())
	val readingBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())
	val completeBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())

	var isWishBookLoaded = false
	var wishBookTotalCountCache = 0L
	var wishBookTotalCount = MutableStateFlow<Long>(0)
	private var wishBookResult = Pager(
		config = PagingConfig(
			pageSize = WISH_TAP_BOOKS_ITEM_LOAD_SIZE,
			enablePlaceholders = false
		),
		pagingSourceFactory = {
			WishBookTapPagingSource(
				bookShelfRepository = bookShelfRepository
			)
		}
	).flow
		.map { pagingData ->
			isWishBookLoaded = true
			pagingData.map { pair ->
				wishBookTotalCountCache = pair.second
				wishBookTotalCount.value = pair.second
				pair.first.getBookShelfDataItem()
			}
		}.cachedIn(viewModelScope)

	var isReadingBookLoaded = false
	var readingBookTotalCountCache = 0L
	var readingBookTotalCount = MutableStateFlow<Long>(0)
	private val readingBookResult by lazy {
		Pager(
			config = PagingConfig(
				pageSize = READING_TAP_BOOKS_ITEM_LOAD_SIZE,
				enablePlaceholders = false
			),
			pagingSourceFactory = {
				ReadingBookTapPagingSource(
					bookShelfRepository = bookShelfRepository
				)
			}
		).flow
			.map { pagingData ->
				isReadingBookLoaded = true
				pagingData.map { pair ->
					readingBookTotalCountCache = pair.second
					readingBookTotalCount.value = pair.second
					pair.first.getBookShelfDataItem()
				}
			}.cachedIn(viewModelScope)
	}

	var isCompleteBookLoaded = false
	var completeBookTotalCountCache = 0L
	var completeBookTotalCount = MutableStateFlow<Long>(0)
	private val completeBookResult by lazy {
		Pager(
			config = PagingConfig(
				pageSize = COMPLETE_TAP_BOOKS_ITEM_LOAD_SIZE,
				enablePlaceholders = false
			),
			pagingSourceFactory = {
				CompleteBookTapPagingSource(
					bookShelfRepository = bookShelfRepository
				)
			}
		).flow
			.map { pagingData ->
				isCompleteBookLoaded = true
				pagingData.map { pair ->
					completeBookTotalCountCache = pair.second
					completeBookTotalCount.value = pair.second
					pair.first.getBookShelfDataItem()
				}
			}.cachedIn(viewModelScope)
	}

	val wishBookCombined by lazy {
		wishBookResult.combine(wishBookModificationEvents) { pagingData, modifications ->
			modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
				.also { renewTotalItemCount(MODIFICATION_EVENT_FLAG_WISH) }
		}.cachedIn(viewModelScope).asLiveData()
	}

	val readingBookCombined by lazy {
		readingBookResult.combine(readingBookModificationEvents) { pagingData, modifications ->
			modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
				.also { renewTotalItemCount(MODIFICATION_EVENT_FLAG_READING) }
		}.cachedIn(viewModelScope).asLiveData()
	}

	val completeBookCombined by lazy {
		completeBookResult.combine(completeBookModificationEvents) { pagingData, modifications ->
			modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
				.also { renewTotalItemCount(MODIFICATION_EVENT_FLAG_COMPLETE) }
		}.cachedIn(viewModelScope).asLiveData()
	}

	fun deleteBookShelfBookWithSwipe(
		bookShelfDataItem: BookShelfDataItem,
		removeEvent: PagingViewEvent.Remove,
		removeWaitingEvent: PagingViewEvent.RemoveWaiting,
		readingStatus: ReadingStatus
	) = viewModelScope.launch {
		runCatching { bookShelfRepository.deleteBookShelfBook(bookShelfDataItem.bookShelfItem.bookShelfId) }
			.onSuccess {
				makeToast(R.string.bookshelf_delete_success)
				addPagingViewEvent(removeEvent, readingStatus)
				removePagingViewEvent(removeWaitingEvent, readingStatus)
			}
			.onFailure {
				makeToast(R.string.bookshelf_delete_fail)
				removePagingViewEvent(removeWaitingEvent, readingStatus)
			}
	}

	fun addPagingViewEvent(pagingViewEvent: PagingViewEvent, readingStatus: ReadingStatus) {
		when (readingStatus) {
			ReadingStatus.WISH -> wishBookModificationEvents.value += pagingViewEvent
			ReadingStatus.READING -> readingBookModificationEvents.value += pagingViewEvent
			ReadingStatus.COMPLETE -> completeBookModificationEvents.value += pagingViewEvent
		}
	}

	fun removePagingViewEvent(pagingViewEvent: PagingViewEvent, readingStatus: ReadingStatus) {
		when (readingStatus) {
			ReadingStatus.WISH -> wishBookModificationEvents.value -= pagingViewEvent
			ReadingStatus.READING -> readingBookModificationEvents.value -= pagingViewEvent
			ReadingStatus.COMPLETE -> completeBookModificationEvents.value -= pagingViewEvent
		}
	}

	private fun applyEvents(
		paging: PagingData<BookShelfDataItem>,
		pagingViewEvent: PagingViewEvent,
	): PagingData<BookShelfDataItem> {
		return when (pagingViewEvent) {
			is PagingViewEvent.Remove -> {
				paging.filter { it.getId() != pagingViewEvent.bookShelfDataItem.getId() }
			}

			is PagingViewEvent.RemoveWaiting -> {
				paging.filter { it.getId() != pagingViewEvent.bookShelfDataItem.getId() }
			}

			is PagingViewEvent.Edit -> {
				paging.map {
					if (pagingViewEvent.bookShelfDataItem.getId() != it.getId()) it
					else pagingViewEvent.bookShelfDataItem
				}
			}
		}
	}

	fun renewTotalItemCount(flag: String) {
		when (flag) {
			MODIFICATION_EVENT_FLAG_WISH -> {
				wishBookTotalCount.value =
					max(wishBookTotalCountCache - getRemoveEventCount(wishBookModificationEvents), 0)
			}

			MODIFICATION_EVENT_FLAG_READING -> {
				readingBookTotalCount.value =
					max(readingBookTotalCountCache - getRemoveEventCount(readingBookModificationEvents), 0)
			}

			MODIFICATION_EVENT_FLAG_COMPLETE -> {
				completeBookTotalCount.value =
					max(completeBookTotalCountCache - getRemoveEventCount(completeBookModificationEvents), 0)
			}
		}
	}

	fun getRemoveWaitingCount(eventFlow: MutableStateFlow<List<PagingViewEvent>>): Int {
		return eventFlow.value.count { it is PagingViewEvent.RemoveWaiting }
	}

	private fun getRemoveEventCount(eventFlow: MutableStateFlow<List<PagingViewEvent>>): Int {
		return eventFlow.value.count {
			(it is PagingViewEvent.Remove) || (it is PagingViewEvent.RemoveWaiting)
		}
	}

	fun startBookShelfUiEvent(event: BookShelfEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun makeToast(stringId: Int) {
		Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
	}

	sealed class PagingViewEvent {
		data class Remove(val bookShelfDataItem: BookShelfDataItem) : PagingViewEvent()
		data class RemoveWaiting(val bookShelfDataItem: BookShelfDataItem) : PagingViewEvent()
		data class Edit(val bookShelfDataItem: BookShelfDataItem) : PagingViewEvent()
	}

	sealed class BookShelfEvent {
		data class ChangeBookShelfTab(val tapIndex: Int) : BookShelfEvent()
	}

	companion object {
		private const val WISH_TAP_BOOKS_ITEM_LOAD_SIZE = 10
		private const val READING_TAP_BOOKS_ITEM_LOAD_SIZE = 4
		private const val COMPLETE_TAP_BOOKS_ITEM_LOAD_SIZE = 4
		const val MODIFICATION_EVENT_FLAG_WISH = "WISH"
		const val MODIFICATION_EVENT_FLAG_READING = "READING"
		const val MODIFICATION_EVENT_FLAG_COMPLETE = "COMPLETE"
		const val WISH_TAB_INDEX = 0
		const val READING_TAB_INDEX = 1
		const val COMPLETE_TAB_INDEX = 2
	}
}