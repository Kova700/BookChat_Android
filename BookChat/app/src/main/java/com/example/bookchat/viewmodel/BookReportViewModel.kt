package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BookReportViewModel(private val bookRepository: BookRepository) :ViewModel() {
    private val _eventFlow = MutableSharedFlow<BookReportUIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    lateinit var book : BookShelfItem

    fun clickBackBtn(){
        startEvent(BookReportUIEvent.MoveToBack)
    }

    private fun startEvent (event : BookReportUIEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class BookReportUIEvent{
        object MoveToBack :BookReportUIEvent()
    }

}