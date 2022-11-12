package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.BookShelfResult
import com.example.bookchat.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WishBookTapViewModel(private val bookRepository: BookRepository) :ViewModel() {

    val wishBookResult = MutableStateFlow<BookShelfResult?>(null)

    init {
        requestGetWishList()
    }

    fun requestGetWishList() = viewModelScope.launch{
        runCatching { bookRepository.requestGetWishList() }
            .onSuccess { bookShelfResult ->
                wishBookResult.value = bookShelfResult
                Toast.makeText(App.instance.applicationContext,"Wish 조회 성공", Toast.LENGTH_SHORT).show()
            }
            .onFailure { Toast.makeText(App.instance.applicationContext,"Wish 조회 실패",Toast.LENGTH_SHORT).show() }
    }

}