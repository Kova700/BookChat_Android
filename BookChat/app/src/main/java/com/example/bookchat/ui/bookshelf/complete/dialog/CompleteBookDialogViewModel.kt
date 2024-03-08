package com.example.bookchat.ui.bookshelf.complete.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.complete.dialog.CompleteBookDialog.Companion.EXTRA_COMPLETE_BOOKSHELF_ITEM_ID
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteBookDialogViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookShelfRepository: BookShelfRepository,
) : ViewModel() {
	private val bookShelfListItemId = savedStateHandle.get<Long>(EXTRA_COMPLETE_BOOKSHELF_ITEM_ID)!!

	private val _eventFlow = MutableSharedFlow<CompleteBookDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState =
		MutableStateFlow<CompleteBookDialogUiState>(CompleteBookDialogUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		getItem()
	}

	private fun getItem() {
		val item =
			bookShelfRepository.getCachedBookShelfItem(bookShelfListItemId)?.toBookShelfListItem()
		item?.let { updateState { copy(completeItem = item) } }
	}

	fun onMoveToBookReportClick() {
		startEvent(CompleteBookDialogEvent.MoveToBookReport)
	}

	fun onMoveToAgonyClick() {
		startEvent(CompleteBookDialogEvent.MoveToAgony)
	}

	private inline fun updateState(block: CompleteBookDialogUiState.() -> CompleteBookDialogUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}

	private fun startEvent(event: CompleteBookDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}