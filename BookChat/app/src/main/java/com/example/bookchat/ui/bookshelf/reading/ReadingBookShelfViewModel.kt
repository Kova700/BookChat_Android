package com.example.bookchat.ui.bookshelf.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfUiState.UiState
import com.example.bookchat.ui.bookshelf.reading.mapper.toReadingBookShelfItems
import com.example.bookchat.ui.bookshelf.reading.model.ReadingBookShelfItem
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
	private val bookShelfRepository: BookShelfRepository,
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
		combine(
			bookShelfRepository.getBookShelfFlow(BookShelfState.READING),
			_isSwiped,
			bookShelfRepository.getBookShelfTotalItemCountFlow(BookShelfState.READING)
		) { items, isSwipedMap, totalCount ->
			items.toReadingBookShelfItems(
				totalItemCount = totalCount,
				isSwipedMap = isSwipedMap
			)
		}.collect { newItems -> updateState { copy(readingItems = newItems) } }
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

	private fun deleteBookShelfItem(bookShelfListItem: ReadingBookShelfItem.Item) =
		viewModelScope.launch {
			runCatching {
				bookShelfRepository.deleteBookShelfBook(
					bookShelfListItem.bookShelfId,
					BookShelfState.READING
				)
			}.onFailure { startEvent(ReadingBookShelfEvent.MakeToast(R.string.bookshelf_delete_fail)) }
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