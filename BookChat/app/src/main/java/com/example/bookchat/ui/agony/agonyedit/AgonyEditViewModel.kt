package com.example.bookchat.ui.agony.agonyedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.ui.agony.agonyrecord.AgonyRecordActivity.Companion.EXTRA_AGONY_ID
import com.example.bookchat.ui.agony.agonyrecord.AgonyRecordActivity.Companion.EXTRA_BOOKSHELF_ITEM_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgonyEditViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val agonyRepository: AgonyRepository,
) : ViewModel() {
	private val bookShelfItemId = savedStateHandle.get<Long>(EXTRA_BOOKSHELF_ITEM_ID)!!
	private val agonyId = savedStateHandle.get<Long>(EXTRA_AGONY_ID)!!

	private val _eventFlow = MutableSharedFlow<AgonyEditUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<AgonyEditUiState>(AgonyEditUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		val originalAgony = agonyRepository.getAgony(
			bookShelfId = bookShelfItemId,
			agonyId = agonyId
		)
		updateState {
			copy(
				agony = originalAgony,
				newTitle = originalAgony.title
			)
		}
	}

	private fun reviseAgony(newTitle: String) = viewModelScope.launch {
		runCatching {
			agonyRepository.reviseAgony(
				bookShelfId = bookShelfItemId,
				agonyId = agonyId,
				newTitle = newTitle
			)
		}
			.onSuccess { startEvent(AgonyEditUiEvent.MoveToBack) }
			.onFailure { startEvent(AgonyEditUiEvent.MakeToast(R.string.agony_title_edit_fail)) }
	}

	fun onClickXBtn() {
		startEvent(AgonyEditUiEvent.MoveToBack)
	}

	fun onClickClearTitleBtn() {
		updateState { copy(newTitle = "") }
	}

	fun onChangeNewTitle(text: String) {
		updateState { copy(newTitle = text.trim()) }
	}

	fun onClickConfirmBtn() {
		if (uiState.value.isPossibleChangeAgony.not()) return
		reviseAgony(uiState.value.newTitle.trim())
	}

	private inline fun updateState(block: AgonyEditUiState.() -> AgonyEditUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: AgonyEditUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}