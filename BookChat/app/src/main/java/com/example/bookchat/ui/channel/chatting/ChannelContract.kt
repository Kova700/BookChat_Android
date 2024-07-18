package com.example.bookchat.ui.channel.chatting

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.data.networkmanager.external.model.NetworkState
import com.example.bookchat.data.stomp.external.model.SocketState
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import com.example.bookchat.ui.channel.drawer.model.ChannelDrawerItem

data class ChannelUiState(
	val uiState: UiState,
	val enteredMessage: String,
	val channel: Channel,
	val client: User,
	val drawerItems: List<ChannelDrawerItem>,
	val chats: List<ChatItem>,
	val newChatNotice: Chat?,
	val socketState: SocketState,
	val networkState: NetworkState,
	val originalLastReadChatId: Long?,
	val isVisibleLastReadChatNotice: Boolean,
	val needToScrollToLastReadChat: Boolean,
	val isFirstConnection: Boolean,
	val olderChatsLoadState: LoadState,
	val newerChatsLoadState: LoadState,
	val isOlderChatFullyLoaded: Boolean,
	val isNewerChatFullyLoaded: Boolean,
	val isCaptureMode: Boolean,
) {
	val isNetworkDisconnected
		get() = networkState == NetworkState.DISCONNECTED

	val isPossibleToLoadOlderChat
		get() = (olderChatsLoadState != LoadState.LOADING)
						&& isOlderChatFullyLoaded.not()
						&& socketState == SocketState.CONNECTED
						&& channel.isAvailableChannel

	val isPossibleToLoadNewerChat
		get() = (newerChatsLoadState != LoadState.LOADING)
						&& isNewerChatFullyLoaded.not()
						&& socketState == SocketState.CONNECTED
						&& channel.isAvailableChannel

	val clientAuthority
		get() = channel.participantAuthorities?.get(client.id)
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
			channel = Channel.DEFAULT,
			client = User.Default,
			uiState = UiState.SUCCESS,
			drawerItems = listOf(ChannelDrawerItem.Header.DEFAULT),
			chats = emptyList(),
			newChatNotice = null,
			socketState = SocketState.DISCONNECTED,
			networkState = NetworkState.DISCONNECTED,
			isFirstConnection = true,
			originalLastReadChatId = null,
			isVisibleLastReadChatNotice = false,
			needToScrollToLastReadChat = false,
			olderChatsLoadState = LoadState.SUCCESS,
			newerChatsLoadState = LoadState.SUCCESS,
			isOlderChatFullyLoaded = false,
			isNewerChatFullyLoaded = true,
			isCaptureMode = false,
		)
	}
}

sealed class ChannelEvent {
	object MoveBack : ChannelEvent()
	object MoveChannelSetting : ChannelEvent()
	object ScrollToBottom : ChannelEvent()
	object OpenOrCloseDrawer : ChannelEvent()

	data class MakeCaptureImage(
		val headerIndex: Int,
		val bottomIndex: Int,
	) : ChannelEvent()

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