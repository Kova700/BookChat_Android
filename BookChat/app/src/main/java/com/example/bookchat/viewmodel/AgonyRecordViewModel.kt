package com.example.bookchat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.bookchat.data.*
import com.example.bookchat.paging.AgonyRecordPagingSource
import com.example.bookchat.repository.AgonyRecordRepository
import com.example.bookchat.utils.Constants.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgonyRecordViewModel @AssistedInject constructor(
    private val agonyRecordRepository: AgonyRecordRepository,
    @Assisted val agonyDataItem: AgonyDataItem
) :ViewModel() {

    private val _eventFlow = MutableSharedFlow<AgonyRecordUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val agonyRecordPagingData = Pager(
        config = PagingConfig(
            pageSize = AGONY_RECORD_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { AgonyRecordPagingSource(agonyDataItem.agony) }
    ).flow
        .map { pagingData -> pagingData.map { agonyRecord -> agonyRecord.getAgonyRecordDataItem() } }
        .cachedIn(viewModelScope)

    val agonyRecordModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())

    val agonyRecordCombined by lazy {
        agonyRecordPagingData.combine(agonyRecordModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    private fun applyEvents(
        paging: PagingData<AgonyRecordDataItem>,
        pagingViewEvent : PagingViewEvent
    ): PagingData<AgonyRecordDataItem> {
        return when(pagingViewEvent){
            is PagingViewEvent.ChangeItemStatusToEdit -> { paging } //임시
            is PagingViewEvent.RemoveItem -> { paging } //임시
        }
    }

    sealed class PagingViewEvent{
        data class ChangeItemStatusToEdit(val agonyRecordItem : AgonyRecordDataItem) : PagingViewEvent()
        data class RemoveItem(val agonyRecordItem : AgonyRecordDataItem) : PagingViewEvent()
    }

    fun clickXbtn(){
        Log.d(TAG, "AgonyRecordViewModel: clickXbtn() - called")
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
        fun create(agonyDataItem: AgonyDataItem) :AgonyRecordViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            agonyDataItem: AgonyDataItem
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(agonyDataItem) as T
            }
        }

        const val AGONY_RECORD_LOAD_SIZE = 4
    }
}