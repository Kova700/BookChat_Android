package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.paging.ChatRoomListPagingSource
import com.example.bookchat.repository.ChatRoomListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomListViewModel @Inject constructor(
    private val chatRoomListRepository: ChatRoomListRepository
) : ViewModel(){

    private val _eventFlow = MutableSharedFlow<ChatRoomListUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val chatRoomPagingData = Pager(
        config = PagingConfig(
            pageSize = CHAT_ROOM_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ChatRoomListPagingSource() }
    ).flow

    fun clickPlusBtn(){
        startEvent(ChatRoomListUiEvent.MoveToMakeChatRoomPage)
    }

    private fun startEvent(event :ChatRoomListUiEvent) = viewModelScope.launch{
        _eventFlow.emit(event)
    }

    sealed class ChatRoomListUiEvent {
        object MoveToMakeChatRoomPage :ChatRoomListUiEvent()
        object MoveToSearchChatRoomPage :ChatRoomListUiEvent()
    }

    companion object{
        private const val CHAT_ROOM_LOAD_SIZE = 6
    }
}