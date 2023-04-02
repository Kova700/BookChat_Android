package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.Book
import com.example.bookchat.repository.ChatRoomListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MakeChatRoomViewModel @Inject constructor(
    private val chatRoomListRepository: ChatRoomListRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<MakeChatRoomUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val chatRoomTitle = MutableStateFlow<String>("")
    val chatRoomTag = MutableStateFlow<String>("")
    val selectedBook = MutableStateFlow<Book?>(null)

    fun clickDeleteTextBtn() {
        chatRoomTitle.value = ""
    }

    fun clickXBtn() {
        startEvent(MakeChatRoomUiEvent.MoveToBack)
    }

    fun clickSelectBookBtn() {
        startEvent(MakeChatRoomUiEvent.MoveSelectBook)
    }

    fun clickDeletSelectBookBtn(){
        selectedBook.value = null
    }

    private fun startEvent(event: MakeChatRoomUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class MakeChatRoomUiEvent {
        object MoveToBack : MakeChatRoomUiEvent()
        object MoveSelectBook : MakeChatRoomUiEvent()
    }
}