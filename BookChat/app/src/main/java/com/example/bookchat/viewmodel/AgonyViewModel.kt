package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.bookchat.data.AgonyFirstItem
import com.example.bookchat.data.AgonyHeader
import com.example.bookchat.data.AgonyItem
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.paging.AgonyPagingSource
import com.example.bookchat.repository.AgonyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgonyViewModel @AssistedInject constructor(
    private val agonyRepository :AgonyRepository,
    @Assisted val book : BookShelfItem
    ) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<AgonizeUIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val agonyModificationEvents = MutableStateFlow<List<PagingViewEvent>>(
        listOf(PagingViewEvent.InsertFirstItem, PagingViewEvent.InsertHeaderItem)
    )

    //AgonyPagingSource에서 Agony를 받아와서 맨앞에 HEADER랑 FIRST를 넣어줘야함 그래야 Adapter에서 데이터 받을 수 있음
    val agonyPagingData = Pager(
        config = PagingConfig(
            pageSize = AGONY_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { AgonyPagingSource() }
    ).flow
        .map { pagingData ->
            pagingData.map { agony ->
                agony.getAgonyDataItem()
            }
        }
        .cachedIn(viewModelScope)

    val agonyCombined by lazy {
        agonyPagingData.combine(agonyModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    private fun applyEvents(
        paging: PagingData<AgonyItem>,
        pagingVeiwEvent :PagingViewEvent
    ): PagingData<AgonyItem>{
        return when(pagingVeiwEvent) {
            is PagingViewEvent.InsertHeaderItem -> {
                paging.insertHeaderItem(item = AgonyHeader(book))
            }
            is PagingViewEvent.InsertFirstItem -> {
                paging.insertHeaderItem(item = AgonyFirstItem(book))
            }
            is PagingViewEvent.RemoveItem -> { paging } //임시
        }
    }

    fun onPagingViewEvent(pagingViewEvent :PagingViewEvent){
        agonyModificationEvents.value += pagingViewEvent
    }

    fun clickBackBtn(){
        startEvent(AgonizeUIEvent.MoveToBack)
    }

    fun renewAgonyList(){
        startEvent(AgonizeUIEvent.RenewAgonyList)
    }

    private fun startEvent (event : AgonizeUIEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class PagingViewEvent{
        object InsertHeaderItem : PagingViewEvent()
        object InsertFirstItem : PagingViewEvent()
        object RemoveItem : PagingViewEvent()
    }

    sealed class AgonizeUIEvent{
        object MoveToBack :AgonizeUIEvent()
        object RenewAgonyList :AgonizeUIEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem) :AgonyViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            book: BookShelfItem
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(book) as T
            }
        }

        const val AGONY_LOAD_SIZE = 6
    }
}