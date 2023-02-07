package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.request.RequestRegisterBookShelfBook
import com.example.bookchat.utils.ReadingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class WishBookTapDialogViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val bookShelfDataItem : BookShelfDataItem
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<WishBookEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var isToggleChecked = MutableStateFlow<Boolean>(true)

    fun requestToggleApi() = viewModelScope.launch {
        isToggleChecked.value = !(isToggleChecked.value)
        if(isToggleChecked.value){
            requestAddWishBook()
            return@launch
        }
        requestRemoveWishBook()
    }

    //알림 내용 스낵바로 수정 예정
    private suspend fun requestRemoveWishBook()= viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(bookShelfDataItem.bookShelfItem.bookShelfId) }
            .onSuccess {
                makeToast("독서예정에서 삭제되었습니다.")
                startEvent(WishBookEvent.RemoveItem)
            }
            .onFailure { makeToast("독서예정 삭제를 실패했습니다.") }
    }

    private suspend fun requestAddWishBook()= viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(bookShelfDataItem.bookShelfItem.getBook(),ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess { makeToast("독서예정에 등록되었습니다.") }
            .onFailure { makeToast("독서예정 등록을 실패했습니다.") }
    }

    fun changeToReadingBook() = viewModelScope.launch{
        runCatching { bookRepository.changeBookShelfBookStatus(bookShelfDataItem.bookShelfItem, ReadingStatus.READING) }
            .onSuccess {
                makeToast("독서중으로 변경되었습니다.")
                startEvent(WishBookEvent.MoveToReadingBook)
            }
            .onFailure { makeToast("독서중으로 변경을 실패했습니다.") }
    }

    private fun startEvent (event : WishBookEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    sealed class WishBookEvent {
        object RemoveItem :WishBookEvent()
        object MoveToReadingBook :WishBookEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(bookShelfDataItem: BookShelfDataItem) :WishBookTapDialogViewModel
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