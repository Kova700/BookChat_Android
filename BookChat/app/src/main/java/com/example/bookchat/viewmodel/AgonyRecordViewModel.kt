package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.bookchat.data.*
import com.example.bookchat.paging.AgonyPagingSource
import com.example.bookchat.paging.AgonyRecordPagingSource
import com.example.bookchat.repository.AgonyRecordRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgonyRecordViewModel @AssistedInject constructor(
    private val agonyRecordRepository: AgonyRecordRepository,
    @Assisted val agony: Agony
) :ViewModel() {

    private val _eventFlow = MutableSharedFlow<AgonyRecordUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val agonyRecordPagingData = Pager(
        config = PagingConfig(
            pageSize = AGONY_RECORD_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { AgonyRecordPagingSource(agony) }
    ).flow
        .map { pagingData ->
            pagingData.map { agonyRecord ->
                agonyRecord.getAgonyRecordDataItem()
            }
        }
        .cachedIn(viewModelScope)

    val agonyRecordModificationEvents = MutableStateFlow<List<PagingViewEvent>>(
        listOf(PagingViewEvent.InsertFirstItem, PagingViewEvent.InsertHeaderItem)
    )

    val agonyRecordCombined by lazy {
        agonyRecordPagingData.combine(agonyRecordModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    private fun applyEvents(
        paging: PagingData<AgonyRecordItem>,
        pagingViewEvent : PagingViewEvent
    ): PagingData<AgonyRecordItem> {
        return when(pagingViewEvent){
            is PagingViewEvent.InsertHeaderItem -> {
                paging.insertHeaderItem(item = AgonyRecordHeader(agony))
            }
            is PagingViewEvent.InsertFirstItem -> {
                paging.insertHeaderItem(item = AgonyRecordFirstItem(agony))
            }
            is PagingViewEvent.ChangeItemStatusToEdit -> { paging } //임시
            is PagingViewEvent.RemoveItem -> { paging } //임시
        }
    }

    sealed class PagingViewEvent{
        object InsertHeaderItem : PagingViewEvent()
        object InsertFirstItem : PagingViewEvent()
        //추후 FirstItem도 statusToEdit가 적용이 가능하게 해야함
        data class ChangeItemStatusToEdit(val agonyRecordItem : AgonyRecordItem) : PagingViewEvent()
        data class RemoveItem(val agonyRecordItem : AgonyRecordItem) : PagingViewEvent()
    }

    fun clickBackBtn(){
        startEvent(AgonyRecordUiEvent.MoveToBack)
    }

    private fun startEvent (event : AgonyRecordUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class AgonyRecordUiEvent{
        object MoveToBack :AgonyRecordUiEvent()
        object RenewAgonyList :AgonyRecordUiEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(agony: Agony) :AgonyRecordViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            agony: Agony
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(agony) as T
            }
        }

        const val AGONY_RECORD_LOAD_SIZE = 4
    }
}