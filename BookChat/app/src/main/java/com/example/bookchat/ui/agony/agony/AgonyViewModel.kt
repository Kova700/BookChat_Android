package com.example.bookchat.ui.agony.agony

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.agony.agony.AgonyUiState.UiState
import com.example.bookchat.ui.agony.agony.mapper.toAgonyListItem
import com.example.bookchat.ui.agony.agony.model.AgonyListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgonyViewModel @Inject constructor(
	private val agonyRepository: AgonyRepository,
	private val bookShelfRepository: BookShelfRepository,
	private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
	private val bookShelfItemId =
		savedStateHandle.get<Long>(EXTRA_AGONY_BOOKSHELF_ITEM_ID)!!

	private val _eventFlow = MutableSharedFlow<AgonyEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<AgonyUiState>(AgonyUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private val _isSelected = MutableStateFlow<Map<Long, Boolean>>(emptyMap()) //(agonyId, isSelected)

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		updateState {
			copy(
				bookshelfItem = bookShelfRepository.getCachedBookShelfItem(bookShelfItemId)
			)
		}
		observeAgonies()
		getAgonies()
	}

	private fun observeAgonies() = viewModelScope.launch {
		combine(agonyRepository.getAgoniesFlow(true), _isSelected) { items, isSelectedMap ->
			items.toAgonyListItem(
				bookshelfItem = uiState.value.bookshelfItem,
				isSelectedMap = isSelectedMap
			)
		}.collect { updateState { copy(agonies = it) } }
	}

	private fun getAgonies() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { agonyRepository.getAgonies(bookShelfItemId) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { handleError(it) }
	}

	fun loadNextAgonies(lastVisibleItemPosition: Int) {
		if (uiState.value.agonies.size - 1 > lastVisibleItemPosition ||
			uiState.value.uiState == UiState.LOADING
		) return
		getAgonies()
	}

	private fun deleteAgonies() = viewModelScope.launch {
		if (uiState.value.uiState == UiState.LOADING) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			agonyRepository.deleteAgony(bookShelfItemId, _isSelected.value.keys.toList())
		}
			.onSuccess { onClickEditCancelBtn() }
			.onFailure {
				updateState { copy(uiState = UiState.EDITING) }
				startEvent(AgonyEvent.MakeToast(R.string.agony_delete_fail))
			}
	}

	fun onClickEditBtn() {
		updateState { copy(uiState = UiState.EDITING) }
		startEvent(AgonyEvent.RenewItemViewMode)
	}

	fun onClickDeleteBtn() {
		deleteAgonies()
	}

	fun onClickEditCancelBtn() {
		updateState { copy(uiState = UiState.SUCCESS) }
		clearAllSelectedItem()
		startEvent(AgonyEvent.RenewItemViewMode)
	}

	private fun clearAllSelectedItem() {
		_isSelected.update { emptyMap() }
	}

	fun onClickBackBtn() {
		startEvent(AgonyEvent.MoveToBack)
	}

	fun onClickFirstItem() {
		if (uiState.value.uiState == UiState.EDITING) return

		startEvent(
			AgonyEvent.OpenBottomSheetDialog(
				bookshelfItemId = bookShelfItemId
			)
		)
	}

	fun onClickItem(item: AgonyListItem.Item) {
		startEvent(
			AgonyEvent.MoveToAgonyRecord(
				bookshelfItemId = bookShelfItemId,
				agonyListItemId = item.agonyId
			)
		)
	}

	fun onItemSelect(item: AgonyListItem.Item) {
		if (item.isSelected) _isSelected.update { _isSelected.value - item.agonyId }
		else _isSelected.update { _isSelected.value + (item.agonyId to true) }
	}

	private fun startEvent(event: AgonyEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: AgonyUiState.() -> AgonyUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }
	}

	companion object {
		const val EXTRA_AGONY_BOOKSHELF_ITEM_ID = "EXTRA_AGONIZE_BOOK"
	}
}