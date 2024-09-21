package com.kova700.bookchat.feature.bookshelf.complete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookshelf.complete.CompleteBookShelfUiState.UiState
import com.kova700.bookchat.feature.bookshelf.complete.mapper.toCompleteBookShelfItems
import com.kova700.bookchat.feature.bookshelf.complete.model.CompleteBookShelfItem
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
class CompleteBookShelfViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<CompleteBookShelfEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState =
		MutableStateFlow<CompleteBookShelfUiState>(CompleteBookShelfUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private val _isSwiped = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

	init {
		observeBookShelfItems()
		getBookShelfItems()
	}

	private fun observeBookShelfItems() = viewModelScope.launch {
		combine(
			bookShelfRepository.getBookShelfFlow(BookShelfState.COMPLETE),
			_isSwiped,
			bookShelfRepository.getBookShelfTotalItemCountFlow(BookShelfState.COMPLETE)
		) { items, isSwipedMap, totalCount ->
			items.toCompleteBookShelfItems(
				totalItemCount = totalCount,
				isSwipedMap = isSwipedMap
			)
		}.collect { newItems -> updateState { copy(completeItems = newItems) } }
	}

	private fun getBookShelfItems() = viewModelScope.launch {
		if (uiState.value.uiState != UiState.INIT_LOADING) updateState { copy(uiState = UiState.LOADING) }
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.COMPLETE) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { startEvent(CompleteBookShelfEvent.ShowSnackBar(R.string.error_else)) }
	}

	fun loadNextBookShelfItems(lastVisibleItemPosition: Int) {
		if (uiState.value.completeItems.size - 1 > lastVisibleItemPosition ||
			uiState.value.uiState == UiState.LOADING
		) return
		getBookShelfItems()
	}

	private fun deleteBookShelfItem(bookShelfListItem: CompleteBookShelfItem.Item) =
		viewModelScope.launch {
			runCatching { bookShelfRepository.deleteBookShelfBook(bookShelfListItem.bookShelfId) }
				.onSuccess { startEvent(CompleteBookShelfEvent.ShowSnackBar(R.string.bookshelf_delete_success)) }
				.onFailure { startEvent(CompleteBookShelfEvent.ShowSnackBar(R.string.bookshelf_delete_fail)) }
		}

	fun onItemClick(bookShelfListItem: CompleteBookShelfItem.Item) {
		startEvent(CompleteBookShelfEvent.MoveToCompleteBookDialog(bookShelfListItem))
	}

	fun onItemLongClick(bookShelfListItem: CompleteBookShelfItem.Item, isSwipe: Boolean) {
		_isSwiped.update { _isSwiped.value + (bookShelfListItem.bookShelfId to isSwipe) }
	}

	fun onItemDeleteClick(bookShelfListItem: CompleteBookShelfItem.Item) {
		deleteBookShelfItem(bookShelfListItem)
	}

	private inline fun updateState(block: CompleteBookShelfUiState.() -> CompleteBookShelfUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: CompleteBookShelfEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}