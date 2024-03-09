package com.example.bookchat.ui.bookshelf.wish.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfItem
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.wish.dialog.WishBookDialog.Companion.EXTRA_WISH_BOOKSHELF_ITEM_ID
import com.example.bookchat.ui.bookshelf.wish.dialog.WishBookDialogUiState.UiState
import com.example.bookchat.utils.makeToast
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
		getItem()
	}

	private fun getItem() {
		val item =
			bookShelfRepository.getCachedBookShelfItem(bookShelfListItemId)?.toBookShelfListItem()
		item?.let { updateState { copy(wishItem = item) } }
	}

	fun onHeartToggleClick() {
		if (!isNetworkConnected()) {
			makeToast(R.string.error_network)
			return
		}

		if (uiState.value.isToggleChecked) {
			onItemDeleteClick()
			updateState { copy(isToggleChecked = false) }
			return
		}
		onItemAddClick()
		updateState { copy(isToggleChecked = true) }
	}

	fun onChangeToReadingClick() {
		onChangeStateClick(BookShelfState.READING)
	}

	fun onChangeToCompleteClick() {
		onChangeStateClick(BookShelfState.COMPLETE)
	}

	private fun onItemDeleteClick() {
		if (uiState.value.uiState == UiState.LOADING) return
		deleteWishBookShelfItem(uiState.value.wishItem)
	}

	private fun onItemAddClick() {
		if (uiState.value.uiState == UiState.LOADING) return
		addWishBookShelfItem(uiState.value.wishItem)
	}

	private fun onChangeStateClick(newState: BookShelfState) {
		if (uiState.value.uiState == UiState.LOADING) return
		changeBookShelfItemStatus(uiState.value.wishItem, newState)
	}

	private fun addWishBookShelfItem(bookShelfListItem: BookShelfListItem) {
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching {
				bookShelfRepository.registerBookShelfBook(
					book = bookShelfListItem.book,
					bookShelfState = BookShelfState.WISH,
				)
			}.onFailure { makeToast(R.string.wish_bookshelf_register_fail) }
				.also { updateState { copy(uiState = UiState.SUCCESS) } }
		}
	}

	private fun deleteWishBookShelfItem(bookShelfListItem: BookShelfListItem) {
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching {
				bookShelfRepository.deleteBookShelfBook(
					bookShelfListItem.bookShelfId,
					BookShelfState.WISH
				)
			}.onFailure { makeToast(R.string.bookshelf_delete_fail) }
				.also { updateState { copy(uiState = UiState.SUCCESS) } }
		}
	}

	private fun changeBookShelfItemStatus(
		bookShelfItem: BookShelfListItem,
		newState: BookShelfState
	) {
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching {
				bookShelfRepository.changeBookShelfBookStatus(
					bookShelfItemId = bookShelfItem.bookShelfId,
					newBookShelfItem = bookShelfItem.copy(state = newState).toBookShelfItem(),
				)
			}.onSuccess {
				startEvent(
					WishBookDialogEvent.ChangeBookShelfTab(
						targetState = newState
					)
				)
			}.onFailure { makeToast(R.string.bookshelf_state_change_fail) }
				.also { updateState { copy(uiState = UiState.SUCCESS) } }
		}
	}

	private inline fun updateState(block: WishBookDialogUiState.() -> WishBookDialogUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}

	private fun startEvent(event: WishBookDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}
}