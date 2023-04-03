package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.bookchat.App
import com.example.bookchat.data.User
import com.example.bookchat.paging.ChatRoomListPagingSource
import com.example.bookchat.paging.ReadingBookTapPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    val user = MutableStateFlow<User>(App.instance.getCachedUser())

    val readingBookResult by lazy {
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ReadingBookTapPagingSource() }
        ).flow
            .map { pagingData ->
                pagingData.map { pair ->
                    pair.first.getBookShelfDataItem()
                }
            }.cachedIn(viewModelScope)
    }

    val chatRoomPagingData = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ChatRoomListPagingSource() }
    ).flow
        .cachedIn(viewModelScope)
}