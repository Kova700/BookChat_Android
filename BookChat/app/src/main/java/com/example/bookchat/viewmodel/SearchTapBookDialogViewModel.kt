package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.RequestRegisterWishBook
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.Constants.TAG
import kotlinx.coroutines.launch

class SearchTapBookDialogViewModel(private val bookRepository: BookRepository) : ViewModel() {
    lateinit var book: Book

    fun requestRegisterWishBook() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: requestRegisterWishBook() - called")
        val requestRegisterWishBook = RequestRegisterWishBook(book)
        runCatching { bookRepository.registerWishBook(requestRegisterWishBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Wish등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Wish등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

}