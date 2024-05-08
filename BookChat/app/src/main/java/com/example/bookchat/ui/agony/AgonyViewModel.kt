package com.example.bookchat.ui.agony

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.Agony
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.agony.AgonyUiState.UiState
import com.example.bookchat.ui.agony.mapper.toAgonyListItem
import com.example.bookchat.ui.agony.model.AgonyListItem
import com.example.bookchat.ui.bookshelf.reading.dialog.ReadingBookDialog
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
	private val bookShelfListItemId =
		savedStateHandle.get<Long>(ReadingBookDialog.EXTRA_AGONY_BOOKSHELF_ITEM_ID)!!

	private val _eventFlow = MutableSharedFlow<AgonyEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<AgonyUiState>(AgonyUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private val _isSelected = MutableStateFlow<Map<Long, Boolean>>(emptyMap()) //(agonyId, isSelected)

	init {
		getItem()
		observeAgonies()
		getAgonies()
	}

	private fun getItem() {
		val item =
			bookShelfRepository.getCachedBookShelfItem(bookShelfListItemId)
		item?.let { updateState { copy(bookshelfItem = item) } }
	}

	private fun observeAgonies() = viewModelScope.launch {
		agonyRepository.getAgoniesFlow(true).combine(_isSelected) { items, isSelectedMap ->
			groupItems(items, isSelectedMap)
		}.collect { newAgonies -> updateState { copy(agonies = newAgonies) } }
	}

	private fun groupItems(
		agonies: List<Agony>,
		isSelectedMap: Map<Long, Boolean>
	): List<AgonyListItem> {
		val groupedItems = mutableListOf<AgonyListItem>()
		groupedItems.add(AgonyListItem.Header(uiState.value.bookshelfItem))
		groupedItems.add(AgonyListItem.FirstItem)
		groupedItems.addAll(agonies.map { it.toAgonyListItem(isSelectedMap[it.agonyId] ?: false) })
		return groupedItems
	}

	private fun getAgonies() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { agonyRepository.getAgonies(bookShelfListItemId) }
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
		runCatching {
			agonyRepository.deleteAgony(bookShelfListItemId, _isSelected.value.keys.toList())
		}
			.onSuccess { clickEditCancelBtn() }
			.onFailure { startEvent(AgonyEvent.MakeToast(R.string.agony_delete_fail)) }
	}

	fun onEditBtnClick() {
		updateState { copy(uiState = UiState.EDITING) }
		startEvent(AgonyEvent.RenewItemViewMode)
	}

	fun clickDeleteBtn() {
		deleteAgonies()
	}

	fun clickEditCancelBtn() {
		updateState { copy(uiState = UiState.SUCCESS) }
		clearAllSelectedItem()
		startEvent(AgonyEvent.RenewItemViewMode)
	}

	private fun clearAllSelectedItem() {
		_isSelected.update { emptyMap() }
	}

	fun clickBackBtn() {
		startEvent(AgonyEvent.MoveToBack)
	}

	fun onFirstItemClick() {
		if (uiState.value.uiState == UiState.EDITING) return

		startEvent(
			AgonyEvent.OpenBottomSheetDialog(
				bookshelfItemId = uiState.value.bookshelfItem.bookShelfId
			)
		)
	}

	fun onItemClick(itemPosition: Int) {
		startEvent(
			AgonyEvent.MoveToAgonyRecord(
				bookshelfItemId = uiState.value.bookshelfItem.bookShelfId,
				agonyListItemId = (uiState.value.agonies[itemPosition] as AgonyListItem.Item).agonyId
			)
		)
	}

	fun onItemSelect(itemPosition: Int) {
		val agony = (uiState.value.agonies[itemPosition] as AgonyListItem.Item)
		if (agony.isSelected) _isSelected.update { _isSelected.value - agony.agonyId }
		else _isSelected.update { _isSelected.value + (agony.agonyId to true) }
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

}