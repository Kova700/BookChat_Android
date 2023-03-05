package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) :ViewModel(){

    private val _eventFlow = MutableSharedFlow<MakeChatRoomUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val chatRoomTitle = MutableStateFlow<String>("")
    val chatRoomTag = MutableStateFlow<String>("")

    fun clickDeleteTextBtn() {
        chatRoomTitle.value = ""
    }

    fun clickXBtn(){
        startEvent(MakeChatRoomUiEvent.MoveToBack)
    }

    private fun startEvent(event : MakeChatRoomUiEvent) = viewModelScope.launch{
        _eventFlow.emit(event)
    }

    sealed class MakeChatRoomUiEvent{
        object MoveToBack :MakeChatRoomUiEvent()
    }
}