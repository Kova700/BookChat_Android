package com.kova700.bookchat.feature.channel.chatting

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import com.kova700.bookchat.feature.channel.chatting.model.ChatItem
import com.kova700.bookchat.feature.channel.drawer.model.ChannelDrawerItem

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
						&& channel.isAvailable

	val isPossibleToLoadNewerChat
		get() = (newerChatsLoadState != LoadState.LOADING)
						&& isNewerChatFullyLoaded.not()
						&& socketState == SocketState.CONNECTED
						&& channel.isAvailable

	val clientAuthority
		get() = channel.participantAuthorities?.get(client.id)
			?: ChannelMemberAuthority.GUEST

	val isClientHost
		get() = clientAuthority == ChannelMemberAuthority.HOST

	val isInitLoading
		get() = uiState == UiState.INIT_LOADING

	enum class UiState {
		SUCCESS,
		INIT_LOADING,
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
			client = User.DEFAULT,
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
	data object MoveBack : ChannelEvent()
	data object MoveChannelSetting : ChannelEvent()
	data object ScrollToBottom : ChannelEvent()
	data object OpenOrCloseDrawer : ChannelEvent()

	data class CopyChatToClipboard(
		val message: String,
	) : ChannelEvent()

	data class MakeCaptureImage(
		val headerIndex: Int,
		val bottomIndex: Int,
	) : ChannelEvent()

	data class MoveUserProfile(
		val user: User,
	) : ChannelEvent()

	data class MoveToWholeText(
		val chatId: Long,
	) : ChannelEvent()

	data class ShowChannelExitWarningDialog(
		val isClientHost: Boolean,
	) : ChannelEvent()

	data class ShowServerDisabledDialog(
		val message: String
	) : ChannelEvent()

	data class ShowServerMaintenanceDialog(
		val message: String
	) : ChannelEvent()

	data class NewChatOccurEvent(
		val chat: Chat,
	) : ChannelEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : ChannelEvent()
}