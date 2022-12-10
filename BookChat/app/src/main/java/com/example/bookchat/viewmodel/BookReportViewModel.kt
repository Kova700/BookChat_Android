package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookReportViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val book : BookShelfItem
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<BookReportUIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun clickBackBtn(){
        startEvent(BookReportUIEvent.MoveToBack)
    }

    private fun startEvent (event : BookReportUIEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class BookReportUIEvent{
        object MoveToBack :BookReportUIEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem) :BookReportViewModel
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
    }

}