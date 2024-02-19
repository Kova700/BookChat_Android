package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.bookchat.App
import com.example.bookchat.paging.remotemediator.ChatRoomRemoteMediator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomListViewModel @Inject constructor() : ViewModel() {

    private val _eventFlow = MutableSharedFlow<ChatRoomListUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    @OptIn(ExperimentalPagingApi::class)
    val chatRoomPagingData = Pager(
        config = PagingConfig(
            pageSize = LOCAL_DATA_CHAT_ROOM_LOAD_SIZE,
            enablePlaceholders = false
        ),
        remoteMediator = ChatRoomRemoteMediator(
            database = App.instance.database,
            apiClient = App.instance.bookChatApiClient
        ),
        pagingSourceFactory = { App.instance.database.chatRoomDAO().pagingSource() }
    ).flow

    fun clickPlusBtn() {
        startEvent(ChatRoomListUiEvent.MoveToMakeChatRoomPage)
    }

    private fun startEvent(event: ChatRoomListUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class ChatRoomListUiEvent {
        object MoveToMakeChatRoomPage : ChatRoomListUiEvent()
        object MoveToSearchChatRoomPage : ChatRoomListUiEvent()
    }

    companion object {
        private const val LOCAL_DATA_CHAT_ROOM_LOAD_SIZE = 7
    }
}