package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.RequestRegisterBookShelfBook
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.ReadingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class WishBookTapDialogViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val book : BookShelfItem
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<WishBookEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var isInited = false
    var isToggleChecked = MutableStateFlow<Boolean>(true)

    fun requestToggleApi() = viewModelScope.launch {
        if (!isInited) { isInited = true; return@launch }

        isToggleChecked.value = !(isToggleChecked.value)

        if(isToggleChecked.value){
            requestAddWishBook()
            return@launch
        }
        requestRemoveWishBook()
    }

    //알림 내용 스낵바로 수정 예정
    private suspend fun requestRemoveWishBook()= viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(book.bookId) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext, "도서가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                startEvent(WishBookEvent.RemoveItem)
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext, "WISH 삭제 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun requestAddWishBook()= viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book.getBook(),ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"독서예정에 등록되었습니다.",Toast.LENGTH_SHORT).show()
                startEvent(WishBookEvent.AddItem)
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Wish 등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

    fun changeToReadingBook() = viewModelScope.launch{
        runCatching { bookRepository.changeBookShelfBookStatus(book, ReadingStatus.READING) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"독서중으로 변경되었습니다.",Toast.LENGTH_SHORT).show()
                startEvent(WishBookEvent.MoveToReadingBook)
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"독서중 변경 실패",Toast.LENGTH_SHORT).show()
            }
    }

    private fun startEvent (event : WishBookEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class WishBookEvent {
        object RemoveItem :WishBookEvent()
        object AddItem :WishBookEvent()
        object MoveToReadingBook :WishBookEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem) :WishBookTapDialogViewModel
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