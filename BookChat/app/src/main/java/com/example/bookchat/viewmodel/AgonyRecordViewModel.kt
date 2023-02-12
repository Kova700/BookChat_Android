package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.bookchat.App
import com.example.bookchat.data.*
import com.example.bookchat.paging.AgonyRecordPagingSource
import com.example.bookchat.repository.AgonyRecordRepository
import com.example.bookchat.utils.SearchSortOption
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
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
                agonyDataItem.agony,
                book,
                SearchSortOption.DESC
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
                makeToast("고민기록 등록을 실패했습니다.")
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
                bookShelfId, agonyId, agonyRecordItem.getId(), newTitle, newContent)
        }
            .onSuccess {
                removePagingViewEvent(PagingViewEvent.ItemStatusChange(agonyRecordItem.copy(status = AgonyRecordDataItemStatus.Loading)))
                addPagingViewEvent(PagingViewEvent.Edit(agonyRecordItem.copy(agonyRecord = agonyRecordItem.agonyRecord.copy(agonyRecordTitle = newTitle, agonyRecordContent = newContent))))
                setUiState(AgonyRecordUiState.Default)
                makeToast("고민기록이 수정되었습니다.")
            }
            .onFailure {
                removePagingViewEvent(PagingViewEvent.ItemStatusChange(agonyRecordItem.copy(status = AgonyRecordDataItemStatus.Loading)))
                makeToast("고민기록이 수정을 실패했습니다.")
            }
    }

    fun deleteAgonyRecord(agonyRecordItem: AgonyRecordDataItem) = viewModelScope.launch {
        runCatching {
            agonyRecordRepository.deleteAgonyRecord(bookShelfId, agonyId, agonyRecordItem.getId())
        }
            .onSuccess {
                addPagingViewEvent(PagingViewEvent.Remove(agonyRecordItem))
                makeToast("고민기록이 삭제되었습니다.")
            }
            .onFailure { makeToast("고민기록 삭제를 실패했습니다.") }
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
            makeToast("제목, 내용을 입력해주세요.")
            return
        }
        renewFirstItemUi(AgonyRecordFirstItemStatus.Loading)
        makeAgonyRecord(firstItemTitle.value.trim(), firstItemContent.value.trim())
    }

    fun clickFolderBtn() {
        makeToast("폴더 버튼 클릭 : 아래에서 SlidingWindow 올라 옴")
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

    fun makeToast(text: String) {
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
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