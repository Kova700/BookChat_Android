package com.example.bookchat.ui.agony.makeagony

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.agony.agony.AgonyActivity
import com.example.bookchat.ui.agony.makeagony.MakeAgonyUiState.UiState
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
	private val bookShelfItemId =
		savedStateHandle.get<Long>(AgonyActivity.EXTRA_BOOKSHELF_ID)!!

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

	//TODO : 길이제한 500자로두고 끝에 ...나오게 수정 +
	// 만들때 키보드 좀 어떻게 해보자 글자가 잘 안보임 (바텀시트가 키보드 위로 올라가게 수정이 안되려나)
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
		if (newTitle.length > 30) return
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
}