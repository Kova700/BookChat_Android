package com.kova700.bookchat.feature.bookshelf.wish.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookshelf.wish.dialog.WishBookDialog.Companion.EXTRA_WISH_BOOKSHELF_ITEM_ID
import com.kova700.bookchat.feature.bookshelf.wish.dialog.WishBookDialogUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishBookDialogViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookShelfRepository: BookShelfRepository,
) : ViewModel() {
	private val bookShelfListItemId = savedStateHandle.get<Long>(EXTRA_WISH_BOOKSHELF_ITEM_ID)!!

	private val _eventFlow = MutableSharedFlow<WishBookDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<WishBookDialogUiState>(WishBookDialogUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		val item = bookShelfRepository.getCachedBookShelfItem(bookShelfListItemId)
		updateState { copy(wishItem = item) }
	}

	fun onHeartToggleClick() {
		deleteWishBookShelfItem(uiState.value.wishItem)
	}

	private fun deleteWishBookShelfItem(bookShelfItem: BookShelfItem) {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching { bookShelfRepository.deleteBookShelfBook(bookShelfItem.bookShelfId) }
				.onSuccess { startEvent(WishBookDialogEvent.MoveToBack) }
				.onFailure { startEvent(WishBookDialogEvent.ShowSnackBar(R.string.bookshelf_delete_fail)) }
				.also { updateState { copy(uiState = UiState.SUCCESS) } }
		}
	}

	private fun changeBookShelfItemStatus(
		bookShelfItem: BookShelfItem,
		newState: BookShelfState,
	) {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching {
				bookShelfRepository.changeBookShelfBookStatus(
					bookShelfItemId = bookShelfItem.bookShelfId,
					newBookShelfItem = bookShelfItem.copy(state = newState),
				)
			}
				.onSuccess { startEvent(WishBookDialogEvent.ChangeBookShelfTab(targetState = newState)) }
				.onFailure { startEvent(WishBookDialogEvent.ShowSnackBar(R.string.bookshelf_state_change_fail)) }
				.also { updateState { copy(uiState = UiState.SUCCESS) } }
		}
	}

	fun onChangeToReadingClick() {
		onChangeStateClick(BookShelfState.READING)
	}

	fun onChangeToCompleteClick() {
		onChangeStateClick(BookShelfState.COMPLETE)
	}

	fun onMoveToAgonyClick() {
		startEvent(WishBookDialogEvent.MoveToAgony(bookShelfListItemId))
	}

	private fun onChangeStateClick(newState: BookShelfState) {
		changeBookShelfItemStatus(uiState.value.wishItem, newState)
	}

	private inline fun updateState(block: WishBookDialogUiState.() -> WishBookDialogUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: WishBookDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}