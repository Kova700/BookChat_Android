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
import java.util.*
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
    val chatRoomProfileImage = MutableStateFlow(byteArrayOf())
    val defaultProfileImageType = MutableStateFlow(Random().nextInt(7) + 1)

    fun clickDeleteTextBtn() {
        chatRoomTitle.value = ""
    }

    fun clickXBtn() {
        startEvent(MakeChatRoomUiEvent.MoveToBack)
    }

    fun clickSelectBookBtn() {
        startEvent(MakeChatRoomUiEvent.MoveSelectBook)
    }

    fun clickDeleteSelectBookBtn(){
        selectedBook.value = null
    }

    fun clickImgEditBtn(){
        startEvent(MakeChatRoomUiEvent.OpenGallery)
    }

    private fun startEvent(event: MakeChatRoomUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class MakeChatRoomUiEvent {
        object MoveToBack : MakeChatRoomUiEvent()
        object MoveSelectBook : MakeChatRoomUiEvent()
        object OpenGallery : MakeChatRoomUiEvent()
    }
}