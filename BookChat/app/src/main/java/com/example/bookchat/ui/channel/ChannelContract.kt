package com.example.bookchat.ui.channel

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.SocketState
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.channel.model.chat.ChatItem
import com.example.bookchat.ui.channel.model.drawer.ChannelDrawerItem

data class ChannelUiState(
	val uiState: UiState,              //UI 구분 필요 (메인 로딩 프로그래스바 필요)
	val enteredMessage: String,
	val channel: Channel?,
	val drawerItems: List<ChannelDrawerItem>,
	val chats: List<ChatItem>,
	val newChatNotice: Chat?,
	val socketState: SocketState,
	val originalLastReadChatId: Long?,
	val isVisibleLastReadChatNotice: Boolean,
	val needToScrollToLastReadChat: Boolean,
	val isFirstConnection: Boolean,
	val olderChatsLoadState: LoadState, //UI 구분 필요 (프로그레스바 Item추가) (Stream참고)
	val newerChatsLoadState: LoadState, //UI 구분 필요 (프로그레스바 Item추가) (Stream참고)
	val isOlderChatFullyLoaded: Boolean,
	val isNewerChatFullyLoaded: Boolean,
) {
	val isPossibleToLoadOlderChat
		get() = (olderChatsLoadState != LoadState.LOADING)
						&& isOlderChatFullyLoaded.not()
						&& socketState == SocketState.CONNECTED

	val isPossibleToLoadNewerChat
		get() = (newerChatsLoadState != LoadState.LOADING)
						&& isNewerChatFullyLoaded.not()
						&& socketState == SocketState.CONNECTED

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	enum class LoadState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = ChannelUiState(
			enteredMessage = "",
			channel = null,
			uiState = UiState.SUCCESS,
			drawerItems = listOf(ChannelDrawerItem.Header.DEFAULT),
			chats = emptyList(),
			newChatNotice = null,
			socketState = SocketState.DISCONNECTED,
			isFirstConnection = true,
			originalLastReadChatId = null,
			isVisibleLastReadChatNotice = false,
			needToScrollToLastReadChat = false,
			olderChatsLoadState = LoadState.SUCCESS,
			newerChatsLoadState = LoadState.SUCCESS,
			isOlderChatFullyLoaded = false,
			isNewerChatFullyLoaded = true
		)
	}
}

sealed class ChannelEvent {
	object MoveBack : ChannelEvent()
	data class MoveUserProfile(val user: User) : ChannelEvent()
	object CaptureChannel : ChannelEvent()
	object ScrollToBottom : ChannelEvent()
	object OpenOrCloseDrawer : ChannelEvent()

	data class NewChatOccurEvent(
		val chat: Chat
	) : ChannelEvent()

	data class MakeToast(
		val stringId: Int
	) : ChannelEvent()
}