package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.bookchat.data.Book
import com.example.bookchat.repository.BookRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SearchDetailViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted private val searchKeyword: String
) : ViewModel() {

    var pagingBookData = flowOf<PagingData<Book>>(PagingData.empty())

    init {
        detailSearchBooks(searchKeyword)
    }

    private fun detailSearchBooks(keyword: String) = viewModelScope.launch{
        //각자 책부분 채팅부분 로딩 프로그래스바 만들어서 서로 로딩상태 두개로 구분 해서 결과 기다리는 표시하기
        runCatching { bookRepository.detailSearchBooks(keyword).cachedIn(viewModelScope) }
            .onSuccess { bookPagingData ->
                pagingBookData = bookPagingData
            }
            .onFailure { }
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(searchKeyword: String) :SearchDetailViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            searchKeyword: String
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(searchKeyword) as T
            }
        }
    }

}