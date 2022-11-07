package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WishBookTapViewModel(private val bookRepository: BookRepository) :ViewModel() {

    val wishBookList = MutableStateFlow<List<BookShelfItem>>( listOf() )

    init {
        viewModelScope.launch { requestGetWishList() }
    }

    private suspend fun requestGetWishList(){
        runCatching { bookRepository.requestGetWishList() }
            .onSuccess {
                wishBookList.value = it
                Toast.makeText(App.instance.applicationContext,"Wish 조회 성공", Toast.LENGTH_SHORT).show()
            }
            .onFailure { Toast.makeText(App.instance.applicationContext,"Wish 조회 실패",Toast.LENGTH_SHORT).show() }
    }
}