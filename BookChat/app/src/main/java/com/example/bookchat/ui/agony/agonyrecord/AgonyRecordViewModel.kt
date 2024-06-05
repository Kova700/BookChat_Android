package com.example.bookchat.ui.agony.agonyrecord

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.Agony
import com.example.bookchat.domain.model.AgonyRecord
import com.example.bookchat.domain.repository.AgonyRecordRepository
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.ui.agony.AgonyActivity
import com.example.bookchat.ui.agony.agonyrecord.AgonyRecordeUiState.UiState
import com.example.bookchat.ui.agony.agonyrecord.mapper.toAgonyRecord
import com.example.bookchat.ui.agony.agonyrecord.mapper.toAgonyRecordListItem
import com.example.bookchat.ui.agony.agonyrecord.model.AgonyRecordListItem
import com.example.bookchat.ui.agony.agonyrecord.model.AgonyRecordListItem.Companion.FIRST_ITEM_STABLE_ID
import com.example.bookchat.ui.agony.agonyrecord.model.AgonyRecordListItem.ItemState
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
class AgonyRecordViewModel @Inject constructor(
	private val agonyRecordRepository: AgonyRecordRepository,
	private val agonyRepository: AgonyRepository,
	private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
	private val bookShelfListItemId = savedStateHandle.get<Long>(AgonyActivity.EXTRA_BOOKSHELF_ID)!!
	private val agonyId = savedStateHandle.get<Long>(AgonyActivity.EXTRA_AGONY_ID)!!

	private val _eventFlow = MutableSharedFlow<AgonyRecordEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<AgonyRecordeUiState>(AgonyRecordeUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private val _itemState = MutableStateFlow<Map<Long, ItemState>>(emptyMap()) //(recordId, state)

	init {
		observeAgonyRecords()
		getAgonyRecords()
	}

	private fun observeAgonyRecords() = viewModelScope.launch {
		combine(
			agonyRecordRepository.getAgonyRecordsFlow(true),
			agonyRepository.getAgonyFlow(agonyId),
			_itemState
		) { records, agony, stateMap -> groupItems(records, agony, stateMap) }
			.collect { newRecords -> updateState { copy(records = newRecords) } }
	}

	private fun groupItems(
		records: List<AgonyRecord>,
		agony: Agony,
		stateMap: Map<Long, ItemState>,
	): List<AgonyRecordListItem> {
		val groupedItems = mutableListOf<AgonyRecordListItem>()
		groupedItems.add(AgonyRecordListItem.Header(agony))
		groupedItems.add(
			AgonyRecordListItem.FirstItem(stateMap[FIRST_ITEM_STABLE_ID] ?: ItemState.Success())
		)
		groupedItems.addAll(records.map {
			it.toAgonyRecordListItem(itemState = stateMap[it.recordId] ?: ItemState.Success())
		})
		return groupedItems
	}

	private fun getAgonyRecords() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			agonyRecordRepository.getAgonyRecords(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId
			)
		}.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { handleError(it) }
	}

	fun loadNextAgonyRecords(lastVisibleItemPosition: Int) {
		if (uiState.value.records.size - 1 > lastVisibleItemPosition ||
			uiState.value.uiState == UiState.LOADING
		) return
		getAgonyRecords()
	}

	private fun makeAgonyRecord(title: String, content: String) = viewModelScope.launch {
		updateFirstItemState(ItemState.Loading)
		updateState { copy(isEditing = true) }
		runCatching {
			agonyRecordRepository.makeAgonyRecord(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId,
				title = title,
				content = content
			)
		}.onSuccess {
			updateFirstItemState(ItemState.Success())
			updateState { copy(isEditing = false) }
		}.onFailure {
			startEvent(AgonyRecordEvent.MakeToast(R.string.agony_record_make_fail))
			updateFirstItemState(
				ItemState.Editing(
					titleBeingEdited = title,
					contentBeingEdited = content
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
		updateState { copy(isEditing = true) }
		runCatching {
			agonyRecordRepository.reviseAgonyRecord(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId,
				agonyRecord = recordItem.toAgonyRecord(),
				newTitle = newTitle,
				newContent = newContent
			)
		}
			.onSuccess {
				_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Success()) }
				updateState { copy(isEditing = false) }
			}
			.onFailure { startEvent(AgonyRecordEvent.MakeToast(R.string.agony_record_revise_fail)) }
	}

	private fun deleteAgonyRecord(
		recordItem: AgonyRecordListItem.Item,
	) = viewModelScope.launch {
		runCatching {
			agonyRecordRepository.deleteAgonyRecord(
				bookShelfId = bookShelfListItemId,
				agonyId = agonyId,
				recordId = recordItem.recordId
			)
		}
			.onSuccess {
				_itemState.update { _itemState.value - recordItem.recordId }
			}
			.onFailure { startEvent(AgonyRecordEvent.MakeToast(R.string.agony_record_delete_fail)) }
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
				titleBeingEdited = recordItem.title,
				contentBeingEdited = recordItem.content
			))
		}

		updateState { copy(isEditing = true) }
	}

	fun onItemSwipe(recordItem: AgonyRecordListItem.Item, isSwiped: Boolean) {
		if (recordItem.state !is ItemState.Success) return
		_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Success(isSwiped)) }
	}

	fun onItemEditCancelBtnClick(recordItem: AgonyRecordListItem.Item) {
		_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Success()) }
		updateState { copy(isEditing = false) }
	}

	fun onItemEditFinishBtnClick(recordItem: AgonyRecordListItem.Item) {
		val itemState = recordItem.state as ItemState.Editing

		val titleWithSpacesRemoved = itemState.titleBeingEdited.trim()
		val contentWithSpacesRemoved = itemState.contentBeingEdited.trim()

		if (titleWithSpacesRemoved.isBlank() || contentWithSpacesRemoved.isBlank()) {
			startEvent(AgonyRecordEvent.MakeToast(R.string.title_content_empty))
			return
		}

		val oldTitle = recordItem.title
		val oldContent = recordItem.content
		val isChanged = (oldTitle != titleWithSpacesRemoved) || (oldContent != contentWithSpacesRemoved)
		if (isChanged.not()) {
			_itemState.update { _itemState.value + (recordItem.recordId to ItemState.Success()) }
			updateState { copy(isEditing = false) }
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
		updateState { copy(isEditing = true) }
	}

	fun onFirstItemEditCancelBtnClick() {
		updateFirstItemState(ItemState.Success())
		updateState { copy(isEditing = false) }
	}

	fun onFirstItemEditFinishBtnClick(recordItem: AgonyRecordListItem.FirstItem) {
		if (recordItem.state !is ItemState.Editing) return

		val titleWithSpacesRemoved = recordItem.state.titleBeingEdited.trim()
		val contentWithSpacesRemoved = recordItem.state.contentBeingEdited.trim()

		if (titleWithSpacesRemoved.isBlank() || contentWithSpacesRemoved.isBlank()) {
			startEvent(AgonyRecordEvent.MakeToast(R.string.title_content_empty))
			return
		}

		makeAgonyRecord(titleWithSpacesRemoved, contentWithSpacesRemoved)
	}

	private fun updateFirstItemState(state: ItemState) {
		_itemState.update { _itemState.value + (FIRST_ITEM_STABLE_ID to state) }
	}

	fun onFolderBtnClick() {
		startEvent(AgonyRecordEvent.OpenChattingScrapDialog)
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

	fun clearEditingState() {
		updateState { copy(isEditing = false) }
		_itemState.update { _itemState.value.filter { it.value !is ItemState.Editing } }
	}

	private inline fun updateState(block: AgonyRecordeUiState.() -> AgonyRecordeUiState) {
		_uiState.update { _uiState.value.block() }
	}

	fun startEvent(event: AgonyRecordEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }
	}
}