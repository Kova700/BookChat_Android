package com.kova700.bookchat.feature.agony.agonyrecord

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.agony.external.AgonyRepository
import com.kova700.bookchat.core.data.agonyrecord.external.AgonyRecordRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.agony.agonyrecord.AgonyRecordActivity.Companion.EXTRA_AGONY_ID
import com.kova700.bookchat.feature.agony.agonyrecord.AgonyRecordActivity.Companion.EXTRA_BOOKSHELF_ITEM_ID
import com.kova700.bookchat.feature.agony.agonyrecord.AgonyRecordUiState.UiState
import com.kova700.bookchat.feature.agony.agonyrecord.mapper.groupItems
import com.kova700.bookchat.feature.agony.agonyrecord.mapper.toAgonyRecord
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem.Companion.FIRST_ITEM_STABLE_ID
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem.ItemState
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

@HiltViewModel
class AgonyRecordViewModel @Inject constructor(
	private val agonyRecordRepository: AgonyRecordRepository,
	private val agonyRepository: AgonyRepository,
	private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
	private val bookShelfListItemId = savedStateHandle.get<Long>(EXTRA_BOOKSHELF_ITEM_ID)!!
	private val agonyId = savedStateHandle.get<Long>(EXTRA_AGONY_ID)!!

	private val _eventFlow = MutableSharedFlow<AgonyRecordEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<AgonyRecordUiState>(AgonyRecordUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private val _itemState = MutableStateFlow<Map<Long, ItemState>>(emptyMap()) //(recordId, state)

	init {
		observeAgonyRecords()
		getInitAgonyRecords()
	}

	private fun observeAgonyRecords() = viewModelScope.launch {
		combine(
			agonyRecordRepository.getAgonyRecordsFlow(true),
			agonyRepository.getAgonyFlow(agonyId),
			_itemState,
			uiState.map { it.uiState }.distinctUntilChanged()
		) { records, agony, stateMap, uiState ->
			groupItems(
				records = records,
				agony = agony,
				stateMap = stateMap,
				uiState = uiState
			)
		}.collect { newRecords -> updateState { copy(records = newRecords) } }
	}

	private fun getInitAgonyRecords() = viewModelScope.launch {
		if (uiState.value.isInitLoading) return@launch
		updateState { copy(uiState = UiState.INIT_LOADING) }
		runCatching {
			agonyRecordRepository.getAgonyRecords(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId
			)
		}.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { updateState { copy(uiState = UiState.INIT_ERROR) } }
	}

	private fun getAgonyRecords() = viewModelScope.launch {
		if (uiState.value.isPagingLoading) return@launch
		updateState { copy(uiState = UiState.PAGING_LOADING) }
		runCatching {
			agonyRecordRepository.getAgonyRecords(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId
			)
		}.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { updateState { copy(uiState = UiState.PAGING_ERROR) } }
	}

	fun loadNextAgonyRecords(lastVisibleItemPosition: Int) {
		if (uiState.value.records.size - 1 > lastVisibleItemPosition
			|| uiState.value.isLoading
			|| uiState.value.isPagingError
		) return
		getAgonyRecords()
	}

	private fun makeAgonyRecord(title: String, content: String) = viewModelScope.launch {
		updateFirstItemState(ItemState.Loading)
		runCatching {
			agonyRecordRepository.makeAgonyRecord(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId,
				title = title,
				content = content
			)
		}.onSuccess {
			updateFirstItemState(ItemState.Success())
			updateState { copy(uiState = UiState.SUCCESS) }
		}.onFailure {
			startEvent(AgonyRecordEvent.ShowSnackBar(R.string.agony_record_make_fail))
			updateFirstItemState(
				ItemState.Editing(
					editingTitle = title,
					editingContent = content
				)
			)
		}
	}

	private fun reviseAgonyRecord(
		recordItem: AgonyRecordListItem.Item,
		newTitle: String,
		newContent: String,
	) = viewModelScope.launch {
		_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Loading) }
		runCatching {
			agonyRecordRepository.reviseAgonyRecord(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId,
				agonyRecord = recordItem.toAgonyRecord(),
				newTitle = newTitle,
				newContent = newContent
			)
		}.onSuccess {
			_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Success()) }
			updateState { copy(uiState = UiState.SUCCESS) }
		}.onFailure { startEvent(AgonyRecordEvent.ShowSnackBar(R.string.agony_record_revise_fail)) }
	}

	private fun deleteAgonyRecord(
		recordItem: AgonyRecordListItem.Item,
	) = viewModelScope.launch {
		_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Loading) }
		runCatching {
			agonyRecordRepository.deleteAgonyRecord(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId,
				recordId = recordItem.recordId
			)
		}.onSuccess { _itemState.update { _itemState.value - recordItem.recordId } }
			.onFailure { startEvent(AgonyRecordEvent.ShowSnackBar(R.string.agony_record_delete_fail)) }
	}

	fun onItemClick(recordItem: AgonyRecordListItem.Item) {
		if (recordItem.state !is ItemState.Success) return
		if (recordItem.state.isSwiped) return

		if (uiState.value.isEditing) {
			startEvent(AgonyRecordEvent.ShowEditCancelWarning)
			return
		}

		_itemState.update {
			_itemState.value + (recordItem.recordId to ItemState.Editing(
				editingTitle = recordItem.title,
				editingContent = recordItem.content
			))
		}
		updateState { copy(uiState = UiState.EDITING) }
	}

	fun onItemSwipe(recordItem: AgonyRecordListItem.Item, isSwiped: Boolean) {
		if (recordItem.state !is ItemState.Success) return
		_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Success(isSwiped)) }
	}

	fun onItemEditCancelBtnClick(recordItem: AgonyRecordListItem.Item) {
		_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Success()) }
		updateState { copy(uiState = UiState.SUCCESS) }
	}

	fun onItemEditFinishBtnClick(recordItem: AgonyRecordListItem.Item) {
		val itemState = recordItem.state as ItemState.Editing

		val titleWithSpacesRemoved = itemState.editingTitle.trim()
		val contentWithSpacesRemoved = itemState.editingContent.trim()

		if (titleWithSpacesRemoved.isBlank() || contentWithSpacesRemoved.isBlank()) {
			startEvent(AgonyRecordEvent.ShowSnackBar(R.string.title_content_empty))
			return
		}

		val oldTitle = recordItem.title
		val oldContent = recordItem.content
		val isChanged = (oldTitle != titleWithSpacesRemoved) || (oldContent != contentWithSpacesRemoved)
		if (isChanged.not()) {
			_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Success()) }
			updateState { copy(uiState = UiState.SUCCESS) }
			return
		}

		reviseAgonyRecord(
			recordItem = recordItem,
			newTitle = titleWithSpacesRemoved,
			newContent = contentWithSpacesRemoved
		)
	}

	fun onHeaderEditBtnClick() {
		startEvent(AgonyRecordEvent.MoveToAgonyTitleEdit(agonyId, bookShelfListItemId))
	}

	fun onFirstItemClick() {
		if (uiState.value.isEditing) {
			startEvent(AgonyRecordEvent.ShowEditCancelWarning)
			return
		}
		updateFirstItemState(ItemState.Editing())
		updateState { copy(uiState = UiState.EDITING) }
	}

	fun onFirstItemEditCancelBtnClick() {
		updateFirstItemState(ItemState.Success())
		updateState { copy(uiState = UiState.SUCCESS) }
	}

	fun onFirstItemEditFinishBtnClick(recordItem: AgonyRecordListItem.FirstItem) {
		if (recordItem.state !is ItemState.Editing) return

		val titleWithSpacesRemoved = recordItem.state.editingTitle.trim()
		val contentWithSpacesRemoved = recordItem.state.editingContent.trim()

		if (titleWithSpacesRemoved.isBlank() || contentWithSpacesRemoved.isBlank()) {
			startEvent(AgonyRecordEvent.ShowSnackBar(R.string.title_content_empty))
			return
		}
		makeAgonyRecord(titleWithSpacesRemoved, contentWithSpacesRemoved)
	}

	private fun updateFirstItemState(state: ItemState) {
		_itemState.update { _itemState.value + (FIRST_ITEM_STABLE_ID to state) }
	}

	fun onBackBtnClick() {
		if (uiState.value.isEditing) {
			startEvent(AgonyRecordEvent.ShowEditCancelWarning)
			return
		}
		startEvent(AgonyRecordEvent.MoveToBack)
	}

	fun onItemDeleteBtnClick(recordItem: AgonyRecordListItem.Item) {
		deleteAgonyRecord(recordItem)
	}

	fun onClickWarningDialogOKBtn() {
		clearEditingState()
	}

	fun onClickRetryBtn() {
		getInitAgonyRecords()
	}

	fun onClickPagingRetryBtn() {
		getAgonyRecords()
	}

	private fun clearEditingState() {
		updateState { copy(uiState = UiState.SUCCESS) }
		_itemState.update { _itemState.value.filter { it.value !is ItemState.Editing } }
	}

	private inline fun updateState(block: AgonyRecordUiState.() -> AgonyRecordUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: AgonyRecordEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	companion object {
		const val TITLE_MAX_LENGTH = 500
		const val CONTENT_MAX_LENGTH = 50000
	}
}