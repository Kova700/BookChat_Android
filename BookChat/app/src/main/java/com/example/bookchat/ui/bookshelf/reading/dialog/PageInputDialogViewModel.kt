package com.example.bookchat.ui.bookshelf.reading.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfItem
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import com.example.bookchat.ui.bookshelf.reading.dialog.PageInputBottomSheetDialog.Companion.EXTRA_PAGE_INPUT_ITEM_ID
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
class PageInputDialogViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookShelfRepository: BookShelfRepository,
) : ViewModel() {
	private val bookShelfListItemId = savedStateHandle.get<Long>(EXTRA_PAGE_INPUT_ITEM_ID)!!

	private val _eventFlow = MutableSharedFlow<PageInputDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState =
		MutableStateFlow<PageInputDialogUiState>(PageInputDialogUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	val inputPage = MutableStateFlow<String>("")

	init {
		getItem()
	}

	private fun getItem() {
		val item =
			bookShelfRepository.getCachedBookShelfItem(bookShelfListItemId)?.toBookShelfListItem()
		item?.let {
			updateState { copy(targetItem = item) }
			inputPage.update { item.pages.toString() }
		}
	}

	private fun registerReadingPage() = viewModelScope.launch {
		val newBookShelfListItem = uiState.value.targetItem.copy(pages = inputPage.value.trim().toInt())
		runCatching {
			bookShelfRepository.changeBookShelfBookStatus(
				bookShelfItemId = uiState.value.targetItem.bookShelfId,
				newBookShelfItem = newBookShelfListItem.toBookShelfItem()
			)
		}.onSuccess { startEvent(PageInputDialogEvent.CloseDialog) }
			.onFailure { makeToast(R.string.bookshelf_page_input_fail) }
	}

	fun onClickSubmit() {
		if (uiState.value.targetItem.pages.toString() == inputPage.value.trim()) {
			startEvent(PageInputDialogEvent.CloseDialog)
			return
		}

		registerReadingPage()
	}

	fun inputNumber(num: Int) {
		if (inputPage.value == "0") inputPage.value = ""
		inputPage.value += num.toString()
	}

	fun deleteNumber() {
		if (inputPage.value.length == 1) {
			inputPage.value = "0"
			return
		}
		inputPage.value = inputPage.value.substring(0, inputPage.value.lastIndex)
	}

	private inline fun updateState(block: PageInputDialogUiState.() -> PageInputDialogUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}

	fun startEvent(event: PageInputDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}