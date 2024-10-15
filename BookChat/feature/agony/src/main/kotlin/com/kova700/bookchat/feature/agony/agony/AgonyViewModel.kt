package com.kova700.bookchat.feature.agony.agony

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.agony.external.AgonyRepository
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.agony.agony.AgonyUiState.UiState
import com.kova700.bookchat.feature.agony.agony.mapper.toAgonyListItem
import com.kova700.bookchat.feature.agony.agony.model.AgonyListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : [FixWaiting] 고민폴더 글자 짤림 현상 있음 .. 처리 하던가 하자
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
		getInitAgonies()
	}

	private fun observeAgonies() = viewModelScope.launch {
		combine(
			agonyRepository.getAgoniesFlow(true),
			_isSelected,
			uiState.map { it.uiState }.distinctUntilChanged()
		) { items, isSelectedMap, uiState ->
			items.toAgonyListItem(
				bookshelfItem = _uiState.value.bookshelfItem,
				isSelectedMap = isSelectedMap,
				uiState = uiState
			)
		}.collect { updateState { copy(agonies = it) } }
	}

	private fun getInitAgonies() = viewModelScope.launch {
		if (uiState.value.isInitLoading) return@launch
		updateState { copy(uiState = UiState.INIT_LOADING) }
		runCatching { agonyRepository.getAgonies(bookShelfItemId) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure {
				updateState { copy(uiState = UiState.INIT_ERROR) }
				startEvent(AgonyEvent.ShowSnackBar(R.string.error_else))
			}
	}

	private fun getAgonies() = viewModelScope.launch {
		if (uiState.value.isPagingLoading) return@launch
		updateState { copy(uiState = UiState.PAGING_LOADING) }
		runCatching { agonyRepository.getAgonies(bookShelfItemId) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure {
				updateState { copy(uiState = UiState.PAGING_ERROR) }
				startEvent(AgonyEvent.ShowSnackBar(R.string.error_else))
			}
	}

	fun loadNextAgonies(lastVisibleItemPosition: Int) {
		if (uiState.value.agonies.size - 1 > lastVisibleItemPosition
			|| uiState.value.isLoading
			|| uiState.value.isPagingError
		) return
		getAgonies()
	}

	private fun deleteAgonies() = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		val selectedIds = _isSelected.value.filterValues { it }.keys.toList()
		if (selectedIds.isEmpty()) {
			onClickEditCancelBtn()
			return@launch
		}
		updateState { copy(uiState = UiState.PAGING_LOADING) }
		runCatching {
			agonyRepository.deleteAgony(
				bookShelfId = bookShelfItemId,
				agonyIds = selectedIds
			)
		}
			.onSuccess { onClickEditCancelBtn() }
			.onFailure {
				updateState { copy(uiState = UiState.EDITING) } //요놈 마지막 체크
				startEvent(AgonyEvent.ShowSnackBar(R.string.agony_delete_fail))
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

	fun onClickRetryBtn() {
		getInitAgonies()
	}

	fun onClickPagingRetry() {
		getAgonies()
	}

	fun onClickBackBtn() {
		startEvent(AgonyEvent.MoveToBack)
	}

	fun onClickFirstItem() {
		if (uiState.value.isEditing) return

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

	companion object {
		const val EXTRA_AGONY_BOOKSHELF_ITEM_ID = "EXTRA_AGONIZE_BOOK"
	}
}