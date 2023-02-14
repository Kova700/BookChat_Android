package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.repository.BookRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CompleteBookTapDialogViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val bookShelfDataItem: BookShelfDataItem
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<CompleteBookEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun clickBookReportBtn(){
        startEvent(CompleteBookEvent.OpenBookReport)
    }

    fun clickAgonizeBtn(){
        startEvent(CompleteBookEvent.OpenAgonize)
    }

    private fun startEvent (event : CompleteBookEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class CompleteBookEvent {
        object OpenBookReport : CompleteBookEvent()
        object OpenAgonize :CompleteBookEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(bookShelfDataItem: BookShelfDataItem) :CompleteBookTapDialogViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            bookShelfDataItem: BookShelfDataItem
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(bookShelfDataItem) as T
            }
        }
    }
}