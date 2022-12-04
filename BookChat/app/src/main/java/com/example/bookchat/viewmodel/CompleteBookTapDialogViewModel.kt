package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CompleteBookTapDialogViewModel(private val bookRepository: BookRepository) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<CompleteBookEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    lateinit var book: BookShelfItem

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
}