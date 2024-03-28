package com.example.bookchat.ui.agony

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
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
class MakeAgonyDialogViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val agonyRepository: AgonyRepository,
	private val bookShelfRepository: BookShelfRepository,
) : ViewModel() {
	private val bookShelfListItemId =
		savedStateHandle.get<Long>(AgonyActivity.EXTRA_BOOKSHELF_ID)!!

	private val _eventFlow = MutableSharedFlow<MakeAgonyUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<MakeAgonyUiState>(MakeAgonyUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		getItem()
	}

	private fun getItem() {
		val item =
			bookShelfRepository.getCachedBookShelfItem(bookShelfListItemId)?.toBookShelfListItem()
		item?.let { updateState { copy(bookshelfItem = item) } }
	}

	fun onRegisterBtnClick() {
		if (uiState.value.agonyTitle.isBlank()) {
			makeToast(R.string.agony_make_empty)
			return
		}
		registerAgony(uiState.value.agonyTitle, uiState.value.selectedColor)
	}

	fun onTitleChanged(newTitle: String) {
		if (newTitle.length > 30) return
		updateState { copy(agonyTitle = newTitle.trim()) }
	}

	fun onColorBtnClick(newColor: AgonyFolderHexColor) {
		updateState { copy(selectedColor = newColor) }
	}

	private fun registerAgony(
		title: String,
		hexColorCode: AgonyFolderHexColor
	) = viewModelScope.launch {
		runCatching {
			agonyRepository.makeAgony(
				uiState.value.bookshelfItem.bookShelfId,
				title,
				hexColorCode
			)
		}
			.onSuccess { startEvent(MakeAgonyUiEvent.MoveToBack) }
			.onFailure { makeToast(R.string.agony_make_fail) }
	}

	private inline fun updateState(block: MakeAgonyUiState.() -> MakeAgonyUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: MakeAgonyUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}