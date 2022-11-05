package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.bookchat.data.Book
import com.example.bookchat.repository.BookRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SearchDetailViewModel(
    private val bookRepository: BookRepository,
    private val searchKeyword: String
) : ViewModel() {

    var pagingBookData = flowOf<PagingData<Book>>(PagingData.empty())

    init {
        viewModelScope.launch {
            runCatching { detailSearchBooks(searchKeyword) }
        }
    }

    private fun detailSearchBooks(keyword: String) {
        //각자 책부분 채팅부분 로딩 프로그래스바 만들어서 서로 로딩상태 두개로 구분 해서 결과 기다리는 표시하기
        runCatching { bookRepository.detailSearchBooks(keyword).cachedIn(viewModelScope) }
            .onSuccess { bookPagingData ->
                pagingBookData = bookPagingData
            }
            .onFailure { }
    }

}