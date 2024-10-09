package com.kova700.bookchat.feature.bookshelf.wish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookshelf.wish.WishBookShelfUiState.UiState
import com.kova700.bookchat.feature.bookshelf.wish.mapper.toWishBookShelfItems
import com.kova700.bookchat.feature.bookshelf.wish.model.WishBookShelfItem
import com.kova700.bookchat.util.book.BookImgSizeManager
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
class WishBookShelfViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository,
	private val bookImgSizeManager: BookImgSizeManager,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<WishBookShelfEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<WishBookShelfUiState>(WishBookShelfUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		observeBookShelfItems()
		getInitBookShelfItems()
	}

	private fun observeBookShelfItems() = viewModelScope.launch {
		combine(
			bookShelfRepository.getBookShelfFlow(BookShelfState.WISH),
			bookShelfRepository.getBookShelfTotalItemCountFlow(BookShelfState.WISH),
			uiState.map { it.uiState }.distinctUntilChanged()
		) { items, totalCount, uiState ->
			items.toWishBookShelfItems(
				totalItemCount = totalCount,
				dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(items.size),
				uiState = uiState
			)
		}.collect { items -> updateState { copy(wishItems = items) } }
	}

	private fun getInitBookShelfItems() = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.INIT_LOADING) }
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.WISH) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure {
				updateState { copy(uiState = UiState.INIT_ERROR) }
				startEvent(WishBookShelfEvent.ShowSnackBar(R.string.error_else))
			}
	}

	private fun getBookShelfItems() = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.PAGING_LOADING) }
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.WISH) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure {
				updateState { copy(uiState = UiState.PAGING_ERROR) }
				startEvent(WishBookShelfEvent.ShowSnackBar(R.string.error_else))
			}
	}

	fun loadNextBookShelfItems(lastVisibleItemPosition: Int) {
		if (uiState.value.wishItems.size - 1 > lastVisibleItemPosition
			|| uiState.value.isLoading
			|| uiState.value.isPagingError
		) return
		getBookShelfItems()
	}

	fun onClickInitRetry() {
		getInitBookShelfItems()
	}

	fun onClickPagingRetry() {
		getBookShelfItems()
	}

	fun onItemClick(bookShelfListItem: WishBookShelfItem.Item) {
		startEvent(WishBookShelfEvent.MoveToWishBookDialog(bookShelfListItem))
	}

	fun moveToOtherTab(targetState: BookShelfState) {
		startEvent(WishBookShelfEvent.ChangeBookShelfTab(targetState))
	}

	private inline fun updateState(block: WishBookShelfUiState.() -> WishBookShelfUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: WishBookShelfEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}