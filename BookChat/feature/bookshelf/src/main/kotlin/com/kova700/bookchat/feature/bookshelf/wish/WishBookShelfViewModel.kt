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
		observeBookShelfItems()
		getBookShelfItems()
	}

	private fun observeBookShelfItems() = viewModelScope.launch {
		bookShelfRepository.getBookShelfFlow(BookShelfState.WISH).combine(
			bookShelfRepository.getBookShelfTotalItemCountFlow(BookShelfState.WISH)
		) { items, totalCount ->
			items.toWishBookShelfItems(
				totalItemCount = totalCount,
				dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(items.size),
			)
		}.collect { items -> updateState { copy(wishItems = items) } }
	}

	private fun getBookShelfItems() = viewModelScope.launch {
		if (uiState.value.uiState != UiState.INIT_LOADING) updateState { copy(uiState = UiState.LOADING) }
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.WISH) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { startEvent(WishBookShelfEvent.ShowSnackBar(R.string.error_else)) }
	}

	fun loadNextBookShelfItems(lastVisibleItemPosition: Int) {
		if (uiState.value.wishItems.size - 1 > lastVisibleItemPosition ||
			uiState.value.uiState == UiState.LOADING
		) return
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