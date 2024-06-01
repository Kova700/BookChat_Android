package com.example.bookchat.ui.channel.chatting

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.SocketState
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import com.example.bookchat.ui.channel.drawer.model.ChannelDrawerItem

data class ChannelUiState(
	val uiState: UiState,              //UI 구분 필요 (메인 로딩 프로그래스바 필요)
	val enteredMessage: String,
	val channel: Channel?,
	val client: User,
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
						&& channel?.isAvailableChannel == true

	val isPossibleToLoadNewerChat
		get() = (newerChatsLoadState != LoadState.LOADING)
						&& isNewerChatFullyLoaded.not()
						&& socketState == SocketState.CONNECTED
						&& channel?.isAvailableChannel == true

	val clientAuthority
		get() = channel?.participantAuthorities?.get(client.id)
			?: ChannelMemberAuthority.GUEST

	val isClientHost
		get() = clientAuthority == ChannelMemberAuthority.HOST

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
			client = User.Default,
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
	object MoveChannelSetting : ChannelEvent()
	object CaptureChannel : ChannelEvent()
	object ScrollToBottom : ChannelEvent()
	object OpenOrCloseDrawer : ChannelEvent()

	data class MoveUserProfile(
		val user: User,
	) : ChannelEvent()

	data class ShowChannelExitWarningDialog(
		val clientAuthority: ChannelMemberAuthority,
	) : ChannelEvent()

	data class NewChatOccurEvent(
		val chat: Chat,
	) : ChannelEvent()

	data class MakeToast(
		val stringId: Int,
	) : ChannelEvent()
}