package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.bookchat.App
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.AgonyDataItemStatus
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.paging.AgonyPagingSource
import com.example.bookchat.repository.AgonyRepository
import com.example.bookchat.utils.SearchSortOption
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgonyViewModel @AssistedInject constructor(
    private val agonyRepository: AgonyRepository,
    @Assisted val book: BookShelfItem
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<AgonyUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val activityStateFlow = MutableStateFlow<AgonyActivityState>(AgonyActivityState.Default)
    val agonyModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())

    val agonyPagingData = Pager(
        config = PagingConfig(
            pageSize = AGONY_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { AgonyPagingSource(book, SearchSortOption.DESC) }
    ).flow
        .map { pagingData -> pagingData.map { agony -> agony.getAgonyDataItem() } }
        .cachedIn(viewModelScope)

    val agonyCombined by lazy {
        agonyPagingData.combine(agonyModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    private fun applyEvents(
        paging: PagingData<AgonyDataItem>,
        pagingViewEvent: PagingViewEvent
    ): PagingData<AgonyDataItem> {
        return when (pagingViewEvent) {
            is PagingViewEvent.ChangeAllItemStatusToEditing -> {
                paging.map { agonyItem -> agonyItem.copy(status = AgonyDataItemStatus.Editing) }
            }

            is PagingViewEvent.ChangeItemStatusToSelected -> {
                paging.map { agonyItem ->
                    if (pagingViewEvent.agonyItem != agonyItem) return@map agonyItem
                    return@map agonyItem.copy(status = AgonyDataItemStatus.Selected)
                }
            }

            is PagingViewEvent.RemoveItem -> {
                paging.filter { agonyItem -> pagingViewEvent.agonyItem.getId() != agonyItem.getId() }
            }

            is PagingViewEvent.ChangeItemTitle ->{
                paging.map { agonyItem ->
                    if (pagingViewEvent.agonyItem != agonyItem) return@map agonyItem
                    return@map agonyItem.copy( agony =  agonyItem.agony.copy(title = pagingViewEvent.newTitle))
                }
            }
        }
    }

    fun onPagingViewEvent(pagingViewEvent: PagingViewEvent) {
        if (agonyModificationEvents.value.contains(pagingViewEvent)) {
            agonyModificationEvents.value -= pagingViewEvent
            return
        }
        agonyModificationEvents.value += pagingViewEvent
    }

    private fun getSelectedItemList(): List<AgonyDataItem> {
        return agonyModificationEvents.value
            .filterIsInstance<PagingViewEvent.ChangeItemStatusToSelected>()
            .map { it.agonyItem }
            .sortedBy { it.agony.agonyId }
    }

    private fun changeItemStatusSelectedToRemoved() {
        agonyModificationEvents.value = agonyModificationEvents.value
            .map { pagingEvent ->
                if (pagingEvent !is PagingViewEvent.ChangeItemStatusToSelected) pagingEvent
                else PagingViewEvent.RemoveItem(pagingEvent.agonyItem)
            }
    }

    private fun deleteAgony() = viewModelScope.launch {
        runCatching { agonyRepository.deleteAgony(book.bookShelfId, getSelectedItemList()) }
            .onSuccess {
                changeItemStatusSelectedToRemoved()
                clickCancelBtn()
            }
            .onFailure { makeToast("고민삭제를 실패했습니다.") }
    }

    fun clickEditBtn() {
        activityStateFlow.value = AgonyActivityState.Editing
        onPagingViewEvent(PagingViewEvent.ChangeAllItemStatusToEditing)
    }

    fun clickDeleteBtn() {
        deleteAgony()
    }

    fun clickCancelBtn() {
        activityStateFlow.value = AgonyActivityState.Default
        onPagingViewEvent(PagingViewEvent.ChangeAllItemStatusToEditing)
        clearAllSelectedItem()
    }

    private fun clearAllSelectedItem() {
        agonyModificationEvents.value = agonyModificationEvents.value
            .filter { it !is PagingViewEvent.ChangeItemStatusToSelected }
    }

    fun clickBackBtn() {
        startEvent(AgonyUiEvent.MoveToBack)
    }

    fun renewAgonyList() {
        startEvent(AgonyUiEvent.RenewAgonyList)
    }

    private fun startEvent(event: AgonyUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    sealed class PagingViewEvent {
        object ChangeAllItemStatusToEditing : PagingViewEvent()
        data class ChangeItemStatusToSelected(val agonyItem: AgonyDataItem) : PagingViewEvent()
        data class RemoveItem(val agonyItem: AgonyDataItem) : PagingViewEvent()
        data class ChangeItemTitle(val agonyItem: AgonyDataItem, val newTitle :String) : PagingViewEvent()
    }

    sealed class AgonyUiEvent {
        object MoveToBack : AgonyUiEvent()
        object RenewAgonyList : AgonyUiEvent()
    }

    sealed class AgonyActivityState {
        object Default : AgonyActivityState()
        object Editing : AgonyActivityState()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem): AgonyViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            book: BookShelfItem
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(book) as T
            }
        }

        const val AGONY_LOAD_SIZE = 6
    }
}