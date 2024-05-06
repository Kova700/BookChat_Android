package com.example.bookchat.ui.bookshelf.reading.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.toStarRating
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfItem
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.reading.dialog.ReadingBookDialog.Companion.EXTRA_READING_BOOKSHELF_ITEM_ID
import com.example.bookchat.ui.bookshelf.reading.dialog.ReadingBookDialogUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingBookDialogViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookShelfRepository: BookShelfRepository,
) : ViewModel() {
	private val bookShelfListItemId =
		savedStateHandle.get<Long>(EXTRA_READING_BOOKSHELF_ITEM_ID)!!

	private val _eventFlow = MutableSharedFlow<ReadingBookDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState =
		MutableStateFlow<ReadingBookDialogUiState>(ReadingBookDialogUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		getItem()
	}

	private fun getItem() {
		val item =
			bookShelfRepository.getCachedBookShelfItem(bookShelfListItemId)?.toBookShelfListItem()
		item?.let { updateState { copy(readingItem = item) } }
	}

	fun onChangeStarRating(rating: Float) {
		updateState { copy(starRating = rating) }
	}

	fun onChangeToCompleteClick() {
		if (uiState.value.starRating <= 0) return
		onChangeStateClick(BookShelfState.COMPLETE)
	}

	private fun onChangeStateClick(newState: BookShelfState) {
		if (uiState.value.uiState == UiState.LOADING) return
		changeBookShelfItemStatus(uiState.value.readingItem, newState)
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
					newBookShelfItem = bookShelfItem.copy(
						state = newState,
						star = uiState.value.starRating.toStarRating()
					).toBookShelfItem(),
				)
			}.onSuccess {
				startEvent(
					ReadingBookDialogEvent.ChangeBookShelfTab(
						targetState = newState
					)
				)
			}.onFailure {
				startEvent(ReadingBookDialogEvent.MakeToast(R.string.bookshelf_state_change_fail))
			}
				.also { updateState { copy(uiState = UiState.SUCCESS) } }
		}
	}

	fun onMoveToAgonyClick() {
		startEvent(ReadingBookDialogEvent.MoveToAgony(bookShelfListItemId))
	}

	private inline fun updateState(block: ReadingBookDialogUiState.() -> ReadingBookDialogUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}

	private fun startEvent(event: ReadingBookDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}