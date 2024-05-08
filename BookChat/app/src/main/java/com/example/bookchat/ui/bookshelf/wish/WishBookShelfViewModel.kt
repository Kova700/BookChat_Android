package com.example.bookchat.ui.bookshelf.wish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.wish.WishBookShelfUiState.UiState
import com.example.bookchat.utils.BookImgSizeManager
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
	private val bookImgSizeManager: BookImgSizeManager
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
		) { items, totalCount -> groupWishItems(items, totalCount) }
			.collect { items -> updateState { copy(wishItems = items) } }
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

	private fun groupWishItems(
		listItems: List<BookShelfItem>,
		totalItemCount: Int
	): List<WishBookShelfItem> {
		val groupedWishItems = mutableListOf<WishBookShelfItem>()
		groupedWishItems.add(WishBookShelfItem.Header(totalItemCount))
		groupedWishItems.addAll(listItems.map { WishBookShelfItem.Item(it.toBookShelfListItem()) })
		val dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(listItems.size)
		(0 until dummyItemCount).forEach { i -> groupedWishItems.add(WishBookShelfItem.Dummy(i)) }
		return groupedWishItems
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