package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.ui.dialog.ReadingTapBookDialog
import com.example.bookchat.ui.fragment.ReadingBookTabFragment
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.StarRating
import com.example.bookchat.utils.toStarRating
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

    //서재 등록 API를 호출하고, 등록되었으면
    //바로 현재 UI를 등록된 도서입니다로 수정하기
    private fun checkAlreadyInBookShelf() = viewModelScope.launch {
        runCatching { bookRepository.checkAlreadyInBookShelf(book) }
            .onSuccess { respondCheckInBookShelf ->
                respondCheckInBookShelf?.let {
                    isAlreadyInBookShelf.value = respondCheckInBookShelf
                    isToggleChecked.value = true
                }
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"서재에 존재여부 확인 실패",Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestRegisterWishBook() = viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess { makeToast("[서재] - [독서예정]에 도서가 등록되었습니다.") }
            .onFailure { makeToast("독서예정 등록에 실패했습니다.") }
    }

    fun requestRegisterReadingBook() = viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.READING)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess { makeToast("[서재] - [독서중]에 도서가 등록되었습니다.") }
            .onFailure { makeToast("독서중 등록에 실패했습니다.") }
    }

    private suspend fun requestRemoveWishBook(respondCheckInBookShelf : RespondCheckInBookShelf)= viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(respondCheckInBookShelf.bookId) }
            .onSuccess { makeToast("도서가 삭제되었습니다.") }
            .onFailure { makeToast("도서 삭제를 실패했습니다.") }
    }

    fun clickCompleteBtn(){
        startUiEvent(SearchTapDialogEvent.OpenSetStarsDalog)
    }

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
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