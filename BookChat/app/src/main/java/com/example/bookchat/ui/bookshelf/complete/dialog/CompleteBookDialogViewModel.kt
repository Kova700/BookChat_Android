package com.example.bookchat.ui.bookshelf.complete.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.complete.dialog.CompleteBookDialog.Companion.EXTRA_COMPLETE_BOOKSHELF_ITEM_ID
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
	private val bookShelfItemId = savedStateHandle.get<Long>(EXTRA_COMPLETE_BOOKSHELF_ITEM_ID)!!

	private val _eventFlow = MutableSharedFlow<CompleteBookDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState =
		MutableStateFlow<CompleteBookDialogUiState>(CompleteBookDialogUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		getBookShelfItem()
	}

	private fun getBookShelfItem() {
		bookShelfRepository.getCachedBookShelfItem(bookShelfItemId)
			.also { updateState { copy(completeItem = it) } }
	}

	fun onMoveToBookReportClick() {
		startEvent(CompleteBookDialogEvent.MoveToBookReport(bookShelfItemId))
	}

	fun onMoveToAgonyClick() {
		startEvent(CompleteBookDialogEvent.MoveToAgony(bookShelfItemId))
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