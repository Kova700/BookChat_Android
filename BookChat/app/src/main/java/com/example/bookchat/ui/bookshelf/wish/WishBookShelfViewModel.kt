package com.example.bookchat.ui.bookshelf.wish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.wish.WishBookShelfUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishBookShelfViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<WishBookShelfEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<WishBookShelfUiState>(WishBookShelfUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		observeBookShelfItems()
		observeBookShelfTotalItemCount()
		getBookShelfItems()
	}

	private fun observeBookShelfItems() = viewModelScope.launch {
		bookShelfRepository.getBookShelfFlow(BookShelfState.WISH).collect { items ->
			updateState { copy(wishItems = items.toBookShelfListItem()) }
		}
	}

	private fun observeBookShelfTotalItemCount() = viewModelScope.launch {
		bookShelfRepository.getBookShelfTotalItemCountFlow(BookShelfState.WISH)
			.collect { itemCount ->
				updateState { copy(totalItemCount = itemCount) }
			}
	}

	private fun getBookShelfItems() =
		viewModelScope.launch {
			updateState { copy(uiState = UiState.LOADING) }
			runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.WISH) }
				.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
				.onFailure { handleError(it) }
		}

	fun loadNextBookShelfItems(lastVisibleItemPosition: Int) {
		if (uiState.value.wishItems.size - 1 > lastVisibleItemPosition ||
			uiState.value.uiState == UiState.LOADING
		) return
		getBookShelfItems()
	}

	fun onItemClick(bookShelfListItem: BookShelfListItem) {
		startEvent(WishBookShelfEvent.MoveToWishBookDialog(bookShelfListItem))
	}

	fun moveToOtherTab(targetState: BookShelfState) {
		startEvent(WishBookShelfEvent.ChangeBookShelfTab(targetState))
	}

	private inline fun updateState(block: WishBookShelfUiState.() -> WishBookShelfUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}

	private fun startEvent(event: WishBookShelfEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }

	}

}