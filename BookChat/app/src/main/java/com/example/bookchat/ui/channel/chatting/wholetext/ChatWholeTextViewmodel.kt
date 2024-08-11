package com.example.bookchat.ui.channel.chatting.wholetext

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatWholeTextViewmodel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val chatRepository: ChatRepository,
) : ViewModel() {
	private val chatId = savedStateHandle.get<Long>(EXTRA_CHAT_ID)!!

	private val _eventFlow = MutableSharedFlow<ChatWholeTextEvent>()
	val eventFlow get() = _eventFlow

	private val _uiState = MutableStateFlow<ChatWholeTextUiState>(ChatWholeTextUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		val chat = chatRepository.getChat(chatId)
		updateState { copy(chatMessage = chat.message) }
	}

	fun onClickBackBtn() {
		startEvent(ChatWholeTextEvent.MoveBack)
	}

	private fun startEvent(event: ChatWholeTextEvent) = viewModelScope.launch {
		eventFlow.emit(event)
	}

	private inline fun updateState(block: ChatWholeTextUiState.() -> ChatWholeTextUiState) {
		_uiState.update { _uiState.value.block() }
	}

	companion object {
		const val EXTRA_CHAT_ID = "EXTRA_CHAT_ID"
	}
}