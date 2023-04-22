package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Chat
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.repository.ChatRepository
import com.example.bookchat.utils.Constants.TAG
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.ConnectionException
import org.hildan.krossbow.stomp.MissingHeartBeatException
import org.hildan.krossbow.stomp.StompSession

class ChatRoomViewModel @AssistedInject constructor(
    @Assisted val chatRoomListItem: UserChatRoomListItem,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val eventFlow = MutableSharedFlow<ChatEvent>()

    val inputtedMessage = MutableStateFlow("")
    private lateinit var stompSession: StompSession
    val chatData = MutableStateFlow(listOf<Chat>())
    val errorList = mutableListOf<String>()

    init {
        viewModelScope.launch {
            connectSocket()
            observeChatFlowJob.start()
            observeErrorFlowJob.start()
        }
    }

    private val observeChatFlowJob = viewModelScope.launch(start = CoroutineStart.LAZY) {
        subscribeChatRoom(chatRoomListItem.roomSid).collect { msg ->
            Log.d(TAG, "ChatRoomViewModel: ChatFlow() - called")
            chatData.value = chatData.value + Gson().fromJson(msg, Chat::class.java)
        }
    }

    //예외처리 추가해야함
    private val observeErrorFlowJob = viewModelScope.launch(start = CoroutineStart.LAZY) {
        subscribeErrorResponse().collect {
            Log.d(TAG, "ChatRoomViewModel: ErrorFlow() - $it")
        }
    }

    private suspend fun connectSocket() {
        Log.d(TAG, "ChatRoomViewModel: connectSocket() - called")
        runCatching { chatRepository.getStompSession() }
            .onSuccess { stompSession = it }
            .onFailure { handleError(it) }
    }

    private suspend fun subscribeChatRoom(roomSid: String): Flow<String> {
        return chatRepository.subscribeChatRoom(stompSession, roomSid)
    }

    private suspend fun subscribeErrorResponse(): Flow<String> {
        return chatRepository.subscribeErrorResponse(stompSession)
    }

    fun sendMessage(roomId: Long) = viewModelScope.launch(Dispatchers.IO) {
        chatRepository.sendMessage(stompSession, roomId, inputtedMessage.value)
        inputtedMessage.value = ""
    }

    fun finishActivity() {
        closeSocket()
        startEvent(ChatEvent.MoveBack)
    }

    private fun closeSocket() {
        unSubscribeTopic()
        disconnectSocket()
    }

    private fun unSubscribeTopic() {
        observeChatFlowJob.cancel()
        observeErrorFlowJob.cancel()
    }

    private fun disconnectSocket() = viewModelScope.launch {
        runCatching { stompSession.disconnect() }
            .onFailure { handleError(it) }
    }

    private fun startEvent(event: ChatEvent) = viewModelScope.launch {
        eventFlow.emit(event)
    }

    private fun handleError(throwable: Throwable) {
        Log.d(TAG, "ChatRoomViewModel: handleError() $throwable")
        Toast.makeText(
            App.instance.applicationContext,
            R.string.error_network_error,
            Toast.LENGTH_SHORT
        ).show()

        when (throwable) {
            is MissingHeartBeatException -> {
                //연결 취소하고 다시 재연결해야함
            }
            is ConnectionException -> {
                //연결 취소하고 다시 재연결해야함
            }
            else -> {}
        }
    }

    sealed class ChatEvent {
        object MoveBack : ChatEvent()
        object CaptureChat : ChatEvent()
        object OpenMenu : ChatEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(chatRoomListItem: UserChatRoomListItem): ChatRoomViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            chatRoomListItem: UserChatRoomListItem
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(chatRoomListItem) as T
            }
        }
    }
}