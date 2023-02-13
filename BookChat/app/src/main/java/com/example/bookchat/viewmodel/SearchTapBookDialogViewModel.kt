package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.utils.ReadingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SearchTapBookDialogViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val book :Book
) : ViewModel()
{
    val isAlreadyInBookShelf = MutableStateFlow<RespondCheckInBookShelf?>(null)
    var isToggleChecked = MutableStateFlow<Boolean>(false)

    private val _eventFlow = MutableSharedFlow<SearchTapDialogEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        checkAlreadyInBookShelf()
   }

    fun requestToggleApi() = viewModelScope.launch {
        isToggleChecked.value = !(isToggleChecked.value)

        if(isToggleChecked.value){
            requestRegisterWishBook()
            return@launch
        }
        isAlreadyInBookShelf.value?.let { requestRemoveWishBook(it) }
    }

    private fun checkAlreadyInBookShelf() = viewModelScope.launch {
        runCatching { bookRepository.checkAlreadyInBookShelf(book) }
            .onSuccess { respondCheckInBookShelf ->
                respondCheckInBookShelf?.let {
                    isAlreadyInBookShelf.value = respondCheckInBookShelf
                    isToggleChecked.value = true
                }
            }
    }

    private fun requestRegisterWishBook() = viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess { makeToast(R.string.wish_bookshelf_register_success) }
            .onFailure { makeToast(R.string.wish_bookshelf_register_fail) }
    }

    fun requestRegisterReadingBook() = viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.READING)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess { makeToast(R.string.reading_bookshelf_register_success) }
            .onFailure { makeToast(R.string.reading_bookshelf_register_fail) }
    }

    private suspend fun requestRemoveWishBook(respondCheckInBookShelf : RespondCheckInBookShelf)= viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(respondCheckInBookShelf.bookId) }
            .onSuccess { makeToast(R.string.bookshelf_delete_success) }
            .onFailure { makeToast(R.string.bookshelf_delete_fail) }
    }

    fun clickCompleteBtn(){
        startUiEvent(SearchTapDialogEvent.OpenSetStarsDalog)
    }

    private fun makeToast(stringId :Int){
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }

    private fun startUiEvent(event: SearchTapDialogEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class SearchTapDialogEvent {
        object OpenSetStarsDalog :SearchTapDialogEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: Book) :SearchTapBookDialogViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            book: Book
        ) :ViewModelProvider.Factory = object :ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(book) as T
            }
        }
    }

}