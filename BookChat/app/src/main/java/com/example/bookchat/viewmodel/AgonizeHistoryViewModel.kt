package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.AgonizeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AgonizeHistoryViewModel(private val agonizeRepository :AgonizeRepository) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<AgonizeUIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    lateinit var book :BookShelfItem

    fun clickBackBtn(){
        startEvent(AgonizeUIEvent.MoveToBack)
    }

    private fun startEvent (event : AgonizeUIEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class AgonizeUIEvent{
        object MoveToBack :AgonizeUIEvent()
    }
}