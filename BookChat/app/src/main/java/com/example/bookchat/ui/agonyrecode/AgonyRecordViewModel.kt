package com.example.bookchat.ui.agonyrecode

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.AgonyRecordDataItem
import com.example.bookchat.data.AgonyRecordDataItemStatus
import com.example.bookchat.data.AgonyRecordFirstItemStatus
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.data.paging.AgonyRecordPagingSource
import com.example.bookchat.domain.repository.AgonyRecordRepository
import com.example.bookchat.utils.SearchSortOption
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AgonyRecordViewModel @AssistedInject constructor(
	private val agonyRecordRepository: AgonyRecordRepository,
	@Assisted val agonyDataItem: AgonyDataItem,
	@Assisted val book: BookShelfItem
) : ViewModel() {
	private val bookShelfId = book.bookShelfId
	private val agonyId = agonyDataItem.agony.agonyId

	private val _eventFlow = MutableSharedFlow<AgonyRecordUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	val agonyRecordUiState = MutableStateFlow<AgonyRecordUiState>(AgonyRecordUiState.Default)

	val firstItemState =
		MutableStateFlow<AgonyRecordFirstItemStatus>(AgonyRecordFirstItemStatus.Default)
	val firstItemTitle = MutableStateFlow<String>("")
	val firstItemContent = MutableStateFlow<String>("")

	val agonyRecordModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())

	private val agonyRecordPagingData = Pager(
		config = PagingConfig(
			pageSize = AGONY_RECORD_LOAD_SIZE,
			enablePlaceholders = false
		),
		pagingSourceFactory = {
			AgonyRecordPagingSource(
				agony = agonyDataItem.agony,
				book = book,
				sortOption = SearchSortOption.ID_DESC,
				agonyRecordRepository = agonyRecordRepository
			)
		}
	).flow
		.map { pagingData -> pagingData.map { agonyRecord -> agonyRecord.getAgonyRecordDataItem() } }
		.cachedIn(viewModelScope)

	val agonyRecordCombined by lazy {
		agonyRecordPagingData.combine(agonyRecordModificationEvents) { pagingData, modifications ->
			modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
		}.cachedIn(viewModelScope).asLiveData()
	}

	private fun applyEvents(
		paging: PagingData<AgonyRecordDataItem>,
		pagingViewEvent: PagingViewEvent
	): PagingData<AgonyRecordDataItem> {
		return when (pagingViewEvent) {
			is PagingViewEvent.ItemStatusChange -> {
				paging.map {
					if (pagingViewEvent.agonyRecordItem.getId() != it.getId()) it
					else pagingViewEvent.agonyRecordItem
				}
			}

			is PagingViewEvent.Edit -> {
				paging.map {
					if (pagingViewEvent.agonyRecordItem.getId() != it.getId()) it
					else pagingViewEvent.agonyRecordItem
				}
			}

			is PagingViewEvent.Remove -> {
				paging.filter { pagingViewEvent.agonyRecordItem.getId() != it.getId() }
			}
		}
	}

	fun resetAllEditingItemToDefault() {
		agonyRecordModificationEvents.value =
			agonyRecordModificationEvents.value.filter { it !is PagingViewEvent.ItemStatusChange }
	}

	private fun makeAgonyRecord(title: String, content: String) = viewModelScope.launch {
		runCatching { agonyRecordRepository.makeAgonyRecord(bookShelfId, agonyId, title, content) }
			.onSuccess {
				clearFirstItemData()
				renewFirstItemUi(AgonyRecordFirstItemStatus.Default)
				startEvent(AgonyRecordUiEvent.RefreshDataItemAdapter)
				setUiState(AgonyRecordUiState.Default)
			}
			.onFailure {
				makeToast(R.string.agony_record_make_fail)
				renewFirstItemUi(AgonyRecordFirstItemStatus.Editing)
			}
	}

	fun reviseAgonyRecord(
		agonyRecordItem: AgonyRecordDataItem,
		newTitle: String,
		newContent: String
	) = viewModelScope.launch {
		runCatching {
			agonyRecordRepository.reviseAgonyRecord(
				bookShelfId, agonyId, agonyRecordItem.getId(), newTitle, newContent
			)
		}
			.onSuccess {
				removePagingViewEvent(PagingViewEvent.ItemStatusChange(agonyRecordItem.copy(status = AgonyRecordDataItemStatus.Loading)))
				addPagingViewEvent(
					PagingViewEvent.Edit(
						agonyRecordItem.copy(
							agonyRecord = agonyRecordItem.agonyRecord.copy(
								agonyRecordTitle = newTitle,
								agonyRecordContent = newContent
							)
						)
					)
				)
				setUiState(AgonyRecordUiState.Default)
				makeToast(R.string.agony_record_revise_success)
			}
			.onFailure {
				removePagingViewEvent(PagingViewEvent.ItemStatusChange(agonyRecordItem.copy(status = AgonyRecordDataItemStatus.Loading)))
				makeToast(R.string.agony_record_revise_fail)
			}
	}

	fun deleteAgonyRecord(agonyRecordItem: AgonyRecordDataItem) = viewModelScope.launch {
		runCatching {
			agonyRecordRepository.deleteAgonyRecord(bookShelfId, agonyId, agonyRecordItem.getId())
		}
			.onSuccess {
				addPagingViewEvent(PagingViewEvent.Remove(agonyRecordItem))
				makeToast(R.string.agony_record_revise_success)
			}
			.onFailure { makeToast(R.string.agony_record_revise_fail) }
	}

	fun clickFirstItemXBtn() {
		if (doesFirstItemHaveData()) {
			startEvent(AgonyRecordUiEvent.ShowWarningDialog)
			return
		}
		renewFirstItemUi(AgonyRecordFirstItemStatus.Default)
		setUiState(AgonyRecordUiState.Default)
	}

	fun clickFirstItemSubmitBtn() {
		if (isBlankFirstItemValue()) {
			makeToast(R.string.title_content_empty)
			return
		}
		renewFirstItemUi(AgonyRecordFirstItemStatus.Loading)
		makeAgonyRecord(firstItemTitle.value.trim(), firstItemContent.value.trim())
	}

	fun clickFolderBtn() {
		//폴더 버튼 클릭 : 아래에서 SlidingWindow 올라 옴
	}

	fun clickBackBtn() {
		if (agonyRecordUiState.value == AgonyRecordUiState.Editing) {
			startEvent(AgonyRecordUiEvent.ShowWarningDialog)
			return
		}
		startEvent(AgonyRecordUiEvent.MoveToBack)
	}

	fun clearFirstItemData() {
		firstItemTitle.value = ""
		firstItemContent.value = ""
	}

	fun addPagingViewEvent(pagingViewEvent: PagingViewEvent) {
		agonyRecordModificationEvents.value += pagingViewEvent
	}

	fun removePagingViewEvent(pagingViewEvent: PagingViewEvent) {
		agonyRecordModificationEvents.value -= pagingViewEvent
	}

	fun renewFirstItemUi(newState: AgonyRecordFirstItemStatus) {
		firstItemState.value = newState
		startEvent(AgonyRecordUiEvent.RenewFirstItemAdapter)
	}

	private fun isBlankFirstItemValue(): Boolean =
		firstItemTitle.value.trim().isBlank() || firstItemContent.value.trim().isBlank()

	fun isFirstItemStatusEditing() =
		firstItemState.value == AgonyRecordFirstItemStatus.Editing

	fun doesFirstItemHaveData(): Boolean =
		firstItemTitle.value.isNotBlank() || firstItemContent.value.isNotBlank()

	fun setUiState(state: AgonyRecordUiState) {
		agonyRecordUiState.value = state
	}

	fun makeToast(stringId: Int) {
		Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
	}

	fun startEvent(event: AgonyRecordUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	sealed class PagingViewEvent {
		data class Remove(val agonyRecordItem: AgonyRecordDataItem) : PagingViewEvent()
		data class Edit(val agonyRecordItem: AgonyRecordDataItem) : PagingViewEvent()
		data class ItemStatusChange(val agonyRecordItem: AgonyRecordDataItem) : PagingViewEvent()
	}

	sealed class AgonyRecordUiState {
		object Default : AgonyRecordUiState()
		object Editing : AgonyRecordUiState()
	}

	sealed class AgonyRecordUiEvent {
		object MoveToBack : AgonyRecordUiEvent()
		object ShowWarningDialog : AgonyRecordUiEvent()
		object RenewFirstItemAdapter : AgonyRecordUiEvent()
		object RefreshDataItemAdapter : AgonyRecordUiEvent()
	}

	@dagger.assisted.AssistedFactory
	interface AssistedFactory {
		fun create(
			agonyDataItem: AgonyDataItem,
			book: BookShelfItem
		): AgonyRecordViewModel
	}

	companion object {
		fun provideFactory(
			assistedFactory: AssistedFactory,
			agonyDataItem: AgonyDataItem,
			book: BookShelfItem
		): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return assistedFactory.create(agonyDataItem, book) as T
			}
		}

		const val AGONY_RECORD_LOAD_SIZE = 4
	}
}