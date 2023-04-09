package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
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
        if (!isNetworkConnected()) {
            makeToast(R.string.error_network)
            return@launch
        }
        if(!isToggleChecked.value){
            requestAddWishBook()
            return@launch
        }
        requestRemoveWishBook()
    }

    private suspend fun requestRemoveWishBook()= viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(bookShelfDataItem.bookShelfItem.bookShelfId) }
            .onSuccess {
                makeToast(R.string.bookshelf_delete_wish_book)
                startEvent(WishBookEvent.RemoveItem)
                setToggleValueReverse()
            }
            .onFailure { makeToast(R.string.bookshelf_delete_fail) }
    }

    private suspend fun requestAddWishBook()= viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(bookShelfDataItem.bookShelfItem.getBook(),ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                makeToast(R.string.wish_bookshelf_register_success)
                startEvent(WishBookEvent.AddItem)
                setToggleValueReverse()
            }
            .onFailure { makeToast(R.string.wish_bookshelf_register_fail) }
    }

    fun changeToReadingBook() = viewModelScope.launch{
        runCatching { bookRepository.changeBookShelfBookStatus(bookShelfDataItem.bookShelfItem, ReadingStatus.READING) }
            .onSuccess {
                makeToast(R.string.bookshelf_change_to_reading_success)
                startEvent(WishBookEvent.MoveToReadingBook)
            }
            .onFailure { makeToast(R.string.bookshelf_change_to_reading_fail) }
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun setToggleValueReverse(){
        isToggleChecked.value = !(isToggleChecked.value)
    }

    private fun startEvent (event : WishBookEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    private fun makeToast(stringId :Int){
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }

    sealed class WishBookEvent {
        object RemoveItem :WishBookEvent()
        object AddItem :WishBookEvent()
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