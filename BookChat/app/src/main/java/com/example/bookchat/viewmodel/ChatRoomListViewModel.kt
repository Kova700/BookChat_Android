package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.bookchat.paging.ChatRoomListPagingSource
import com.example.bookchat.repository.ChatRoomListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatRoomListViewModel @Inject constructor(
    private val chatRoomListRepository: ChatRoomListRepository
) : ViewModel(){
    val chatRoomPagingData = Pager(
        config = PagingConfig(
            pageSize = CHAT_ROOM_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ChatRoomListPagingSource() }
    ).flow

    companion object{
        private const val CHAT_ROOM_LOAD_SIZE = 6
    }
}