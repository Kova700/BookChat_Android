package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BookReportViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val book : BookShelfItem
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<BookReportUIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val bookReportStatus = MutableStateFlow(BookReportStatus.Default)

    init {
        //Room사용해서 로컬에 데이터 있으면 가져오고
        //로컬에 데이터 없으면 호출해서 가져옴
            //넘겨받은 값이 없으면 입력창 노출하고
            //넘겨받은 값이 있으면 데이터 노출
    }

    fun clickBackBtn(){
        startEvent(BookReportUIEvent.MoveToBack)
    }

    private fun startEvent (event : BookReportUIEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem) :BookReportViewModel
    }

    sealed class BookReportStatus{
        object Default :BookReportStatus() //기본
        object ShowData :BookReportStatus() //데이터 노출
        object EditData :BookReportStatus() //입력창 노출
        object NoData :BookReportStatus() //입력창 노출
    }

    sealed class BookReportUIEvent{
        object MoveToBack :BookReportUIEvent()
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