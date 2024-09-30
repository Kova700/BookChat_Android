package com.kova700.bookchat.feature.bookshelf.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookshelf.reading.ReadingBookShelfUiState.UiState
import com.kova700.bookchat.feature.bookshelf.reading.mapper.toReadingBookShelfItems
import com.kova700.bookchat.feature.bookshelf.reading.model.ReadingBookShelfItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingBookShelfViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<ReadingBookShelfEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<ReadingBookShelfUiState>(ReadingBookShelfUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private val _isSwiped = MutableStateFlow<Map<Long, Boolean>>(emptyMap()) // (ItemId, isSwiped)

	init {
		initUiState()
	}

	private fun initUiState() {
		updateState { copy(uiState = UiState.INIT_LOADING) }
		observeBookShelfItems()
		getInitBookShelfItems()
	}

	private fun observeBookShelfItems() = viewModelScope.launch {
		combine(
			bookShelfRepository.getBookShelfFlow(BookShelfState.READING),
			_isSwiped,
			bookShelfRepository.getBookShelfTotalItemCountFlow(BookShelfState.READING),
			uiState.map { it.uiState }.distinctUntilChanged()
		) { items, isSwipedMap, totalCount, uiState ->
			items.toReadingBookShelfItems(
				totalItemCount = totalCount,
				isSwipedMap = isSwipedMap,
				uiState = uiState
			)
		}.collect { newItems -> updateState { copy(readingItems = newItems) } }
	}

	fun getInitBookShelfItems() = viewModelScope.launch {
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.READING) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure {
				updateState { copy(uiState = UiState.INIT_ERROR) }
				startEvent(ReadingBookShelfEvent.ShowSnackBar(R.string.error_else))
			}
	}

	fun getBookShelfItems() = viewModelScope.launch {
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.READING) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure {
				updateState { copy(uiState = UiState.PAGING_ERROR) }
				startEvent(ReadingBookShelfEvent.ShowSnackBar(R.string.error_else))
			}
	}

	fun loadNextBookShelfItems(lastVisibleItemPosition: Int) {
		if (uiState.value.readingItems.size - 1 > lastVisibleItemPosition ||
			uiState.value.isLoading
		) return
		updateState { copy(uiState = UiState.PAGING_LOADING) }
		getBookShelfItems()
	}

	private fun deleteBookShelfItem(bookShelfListItem: ReadingBookShelfItem.Item) =
		viewModelScope.launch {
			runCatching { bookShelfRepository.deleteBookShelfBook(bookShelfListItem.bookShelfId) }
				.onSuccess { startEvent(ReadingBookShelfEvent.ShowSnackBar(R.string.bookshelf_delete_success)) }
				.onFailure { startEvent(ReadingBookShelfEvent.ShowSnackBar(R.string.bookshelf_delete_fail)) }
		}


	fun onItemClick(bookShelfListItem: ReadingBookShelfItem.Item) {
		startEvent(ReadingBookShelfEvent.MoveToReadingBookDialog(bookShelfListItem))
	}

	fun onItemLongClick(bookShelfListItem: ReadingBookShelfItem.Item, isSwipe: Boolean) {
		_isSwiped.update { _isSwiped.value + (bookShelfListItem.bookShelfId to isSwipe) }
	}

	fun onPageInputBtnClick(bookShelfListItem: ReadingBookShelfItem.Item) {
		startEvent(ReadingBookShelfEvent.MoveToPageInputDialog(bookShelfListItem))
	}

	fun onItemDeleteClick(bookShelfListItem: ReadingBookShelfItem.Item) {
		deleteBookShelfItem(bookShelfListItem)
	}

	fun moveToOtherTab(targetState: BookShelfState) {
		startEvent(ReadingBookShelfEvent.ChangeBookShelfTab(targetState))
	}

	private inline fun updateState(block: ReadingBookShelfUiState.() -> ReadingBookShelfUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: ReadingBookShelfEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}