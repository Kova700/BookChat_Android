package com.kova700.bookchat.feature.bookshelf.reading.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookshelf.reading.dialog.PageInputBottomSheetDialog.Companion.EXTRA_PAGE_INPUT_ITEM_ID
import com.kova700.bookchat.feature.bookshelf.reading.dialog.PageInputDialogUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PageInputDialogViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookShelfRepository: BookShelfRepository,
) : ViewModel() {
	private val bookShelfListItemId = savedStateHandle.get<Long>(EXTRA_PAGE_INPUT_ITEM_ID)!!

	private val _eventFlow = MutableSharedFlow<PageInputDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<PageInputDialogUiState>(PageInputDialogUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		val item = bookShelfRepository.getCachedBookShelfItem(bookShelfListItemId)
		updateState {
			copy(
				uiState = UiState.SUCCESS,
				targetItem = item,
				inputPage = item.pages.toString()
			)
		}
	}

	private fun registerReadingPage() = viewModelScope.launch {
		if (uiState.value.uiState == UiState.LOADING) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		val newBookShelfItem = uiState.value.targetItem.copy(pages = uiState.value.inputPage.toInt())
		runCatching {
			bookShelfRepository.changeBookShelfBookStatus(
				bookShelfItemId = uiState.value.targetItem.bookShelfId,
				newBookShelfItem = newBookShelfItem
			)
		}
			.onSuccess {
				updateState { copy(uiState = UiState.SUCCESS) }
				startEvent(PageInputDialogEvent.CloseDialog)
			}
			.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(PageInputDialogEvent.ShowSnackBar(R.string.bookshelf_page_input_fail))
			}
	}

	fun onClickSubmit() {
		if (uiState.value.targetItem.pages.toString() == uiState.value.inputPage) {
			startEvent(PageInputDialogEvent.CloseDialog)
			return
		}
		registerReadingPage()
	}

	fun onClickNumberBtn(num: Int) {
		if (uiState.value.inputPage == "0") updateState { copy(inputPage = "") }
		updateState { copy(inputPage = inputPage + num.toString()) }
	}

	fun onClickDeleteNumberBtn() {
		if (uiState.value.inputPage.length == 1) {
			updateState { copy(inputPage = "0") }
			return
		}
		updateState { copy(inputPage = inputPage.substring(0, inputPage.lastIndex)) }
	}

	private inline fun updateState(block: PageInputDialogUiState.() -> PageInputDialogUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: PageInputDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}