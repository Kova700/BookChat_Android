package com.example.bookchat.ui.bookshelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.model.BookShelfState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookShelfViewModel @Inject constructor(
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<BookShelfEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<BookShelfUiState>(BookShelfUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	fun moveToOtherTab(targetState: BookShelfState) {
		startEvent(BookShelfEvent.ChangeBookShelfTab(convertStateToTabIndex(targetState)))
	}

	private fun convertStateToTabIndex(targetState: BookShelfState): Int {
		return when (targetState) {
			BookShelfState.WISH -> WISH_TAB_INDEX
			BookShelfState.READING -> READING_TAB_INDEX
			BookShelfState.COMPLETE -> COMPLETE_TAB_INDEX
		}
	}

	private fun startEvent(event: BookShelfEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	companion object {
		const val WISH_TAB_INDEX = 0
		const val READING_TAB_INDEX = 1
		const val COMPLETE_TAB_INDEX = 2
	}
}