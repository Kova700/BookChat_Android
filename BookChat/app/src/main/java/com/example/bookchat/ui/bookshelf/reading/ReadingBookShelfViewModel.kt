package com.example.bookchat.ui.bookshelf.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfUiState.UiState
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingBookShelfViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<ReadingBookShelfEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<ReadingBookShelfUiState>(ReadingBookShelfUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private val _isSwiped = MutableStateFlow<Map<Long, Boolean>>(emptyMap()) // (ItemId, isSwiped)

	init {
		observeBookShelfItems()
		getBookShelfItems()
	}

	private fun observeBookShelfItems() = viewModelScope.launch {
		bookShelfRepository.getBookShelfFlow(BookShelfState.READING)
			.combine(_isSwiped) { items, isSwipedMap ->
				items.map {
					it.toBookShelfListItem(isSwipedMap[it.bookShelfId] ?: false)
				}
			}.combine(
				bookShelfRepository.getBookShelfTotalItemCountFlow(BookShelfState.READING)
			) { items, totalCount -> groupReadingItems(items, totalCount) }
			.collect { newItems -> updateState { copy(readingItems = newItems) } }
	}

	private fun groupReadingItems(
		readingItems: List<BookShelfListItem>,
		totalItemCount: Int
	): List<ReadingBookShelfItem> {
		val groupedWishItems = mutableListOf<ReadingBookShelfItem>()
		groupedWishItems.add(ReadingBookShelfItem.Header(totalItemCount))
		groupedWishItems.addAll(readingItems.map { ReadingBookShelfItem.Item(it) })
		return groupedWishItems
	}

	private fun getBookShelfItems() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.READING) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { handleError(it) }
	}


	fun loadNextBookShelfItems(lastVisibleItemPosition: Int) {
		if (uiState.value.readingItems.size - 1 > lastVisibleItemPosition ||
			uiState.value.uiState == UiState.LOADING
		) return
		getBookShelfItems()
	}

	private fun deleteBookShelfItem(bookShelfListItem: BookShelfListItem) =
		viewModelScope.launch {
			runCatching {
				bookShelfRepository.deleteBookShelfBook(
					bookShelfListItem.bookShelfId,
					BookShelfState.READING
				)
			}.onFailure { makeToast(R.string.bookshelf_delete_fail) }
		}


	fun onItemClick(bookShelfListItem: BookShelfListItem) {
		startEvent(ReadingBookShelfEvent.MoveToReadingBookDialog(bookShelfListItem))
	}

	fun onItemLongClick(bookShelfListItem: BookShelfListItem, isSwipe: Boolean) {
		_isSwiped.update { _isSwiped.value + (bookShelfListItem.bookShelfId to isSwipe) }
	}

	fun onPageInputBtnClick(bookShelfListItem: BookShelfListItem) {
		startEvent(ReadingBookShelfEvent.MoveToPageInputDialog(bookShelfListItem))
	}

	fun onItemDeleteClick(bookShelfListItem: BookShelfListItem) {
		deleteBookShelfItem(bookShelfListItem)
	}

	fun moveToOtherTab(targetState: BookShelfState) {
		startEvent(ReadingBookShelfEvent.ChangeBookShelfTab(targetState))
	}

	private inline fun updateState(block: ReadingBookShelfUiState.() -> ReadingBookShelfUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}

	private fun startEvent(event: ReadingBookShelfEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }
	}

}