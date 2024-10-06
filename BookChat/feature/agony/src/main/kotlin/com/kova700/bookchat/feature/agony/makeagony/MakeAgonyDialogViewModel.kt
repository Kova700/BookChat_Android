package com.kova700.bookchat.feature.agony.makeagony

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.agony.external.AgonyRepository
import com.kova700.bookchat.core.data.agony.external.model.AgonyFolderHexColor
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.agony.agony.AgonyActivity.Companion.EXTRA_BOOKSHELF_ID
import com.kova700.bookchat.feature.agony.makeagony.MakeAgonyUiState.UiState
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
	private val bookShelfItemId = savedStateHandle.get<Long>(EXTRA_BOOKSHELF_ID)!!

	private val _eventFlow = MutableSharedFlow<MakeAgonyUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<MakeAgonyUiState>(MakeAgonyUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		updateState {
			copy(
				uiState = UiState.SUCCESS,
				bookshelfItem = bookShelfRepository.getCachedBookShelfItem(bookShelfItemId)
			)
		}
	}

	fun onRegisterBtnClick() {
		if (uiState.value.agonyTitle.isBlank()) {
			startEvent(MakeAgonyUiEvent.ShowSnackBar(R.string.agony_make_empty))
			return
		}
		registerAgony(
			title = uiState.value.agonyTitle,
			hexColorCode = uiState.value.selectedColor
		)
	}

	fun onTitleChanged(newTitle: String) {
		if (newTitle.length > MAX_TITLE_LENGTH) return
		updateState { copy(agonyTitle = newTitle.trim()) }
	}

	fun onColorBtnClick(newColor: AgonyFolderHexColor) {
		updateState { copy(selectedColor = newColor) }
	}

	private fun registerAgony(
		title: String,
		hexColorCode: AgonyFolderHexColor,
	) = viewModelScope.launch {
		if (uiState.value.uiState == UiState.LOADING) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			agonyRepository.makeAgony(
				bookShelfId = bookShelfItemId,
				title = title,
				hexColorCode = hexColorCode
			)
		}
			.onSuccess {
				updateState { copy(uiState = UiState.SUCCESS) }
				startEvent(MakeAgonyUiEvent.MoveToBack)
			}
			.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(MakeAgonyUiEvent.ShowSnackBar(R.string.agony_make_fail))
			}
	}

	private inline fun updateState(block: MakeAgonyUiState.() -> MakeAgonyUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: MakeAgonyUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	companion object {
		const val MAX_TITLE_LENGTH = 500
	}
}