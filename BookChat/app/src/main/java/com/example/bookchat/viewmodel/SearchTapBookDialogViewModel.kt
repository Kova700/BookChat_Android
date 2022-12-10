package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.RequestRegisterBookShelfBook
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SearchTapBookDialogViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val book :Book
) : ViewModel()
{
    val isAlreadyInBookShelf = MutableStateFlow(false)

    init {
        //checkAlreadyInBookShelf()
   }

    fun checkAlreadyInBookShelf() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: checkAlreadyInBookShelf() - called")
        runCatching { bookRepository.checkAlreadyInBookShelf(book) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"서재에 존재여부 확인 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"서재에 존재여부 확인 실패",Toast.LENGTH_SHORT).show()
            }
    }

    fun requestRegisterWishBook() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: requestRegisterWishBook() - called")
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Wish등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Wish등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

    fun requestRegisterReadingBook() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: requestRegisterReadingBook() - called")
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book,ReadingStatus.READING)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Reading등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Reading등록 실패",Toast.LENGTH_SHORT).show()
            }
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