package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.ReadingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ReadingBookTapDialogViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val bookShelfDataItem: BookShelfDataItem
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<ReadingBookEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var starRating = MutableStateFlow<Float>(0.0F)

    fun changeToCompleteBook() = viewModelScope.launch{
        bookShelfDataItem.bookShelfItem.setStarRating(starRating.value)
        runCatching { bookRepository.changeBookShelfBookStatus(bookShelfDataItem.bookShelfItem, ReadingStatus.COMPLETE) }
            .onSuccess {
                makeToast("독서완료로 변경되었습니다.")
                startEvent(ReadingBookEvent.MoveToCompleteBook)
            }
            .onFailure { makeToast("독서완료로 변경을 실패했습니다.") }
    }

    fun openAgonizeActivity(){
        startEvent(ReadingBookEvent.OpenAgonize)
    }

    private fun startEvent (event : ReadingBookEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    sealed class ReadingBookEvent {
        object MoveToCompleteBook : ReadingBookEvent()
        object OpenAgonize :ReadingBookEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(bookShelfDataItem: BookShelfDataItem) :ReadingBookTapDialogViewModel
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