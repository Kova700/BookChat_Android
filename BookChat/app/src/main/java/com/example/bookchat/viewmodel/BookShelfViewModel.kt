package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class BookShelfViewModel(private val bookRepository: BookRepository) :ViewModel() {

    var wishBookResult = flowOf<PagingData<BookShelfItem>>(PagingData.empty())
    var wishBookTotalCount = MutableStateFlow<Long>(0)

    var readingBookResult = flowOf<PagingData<BookShelfItem>>(PagingData.empty())
    var readingBookTotalCount = MutableStateFlow<Long>(0)

    init {
        requestGetWishList()
        requestGetReadingList()
    }

    private fun requestGetWishList() = viewModelScope.launch{
        runCatching { bookRepository.requestGetWishList().cachedIn(viewModelScope) }
            .onSuccess { bookShelfResult ->
                wishBookResult = bookShelfResult.map { pagingData ->
                    pagingData.map { pair -> wishBookTotalCount.value = pair.second; pair.first }
                }
                Toast.makeText(App.instance.applicationContext,"Wish 조회 성공", Toast.LENGTH_SHORT).show()
            }
            .onFailure { Toast.makeText(App.instance.applicationContext,"Wish 조회 실패",Toast.LENGTH_SHORT).show() }
    }


    private fun requestGetReadingList() = viewModelScope.launch{
        runCatching { bookRepository.requestGetReadingList().cachedIn(viewModelScope) }
            .onSuccess { bookShelfResult ->
                readingBookResult = bookShelfResult.map { pagingData ->
                    pagingData.map { pair -> readingBookTotalCount.value = pair.second; pair.first }
                }
                Toast.makeText(App.instance.applicationContext,"Reading 조회 성공", Toast.LENGTH_SHORT).show()
            }
            .onFailure { Toast.makeText(App.instance.applicationContext,"Reading 조회 실패", Toast.LENGTH_SHORT).show() }
    }

}