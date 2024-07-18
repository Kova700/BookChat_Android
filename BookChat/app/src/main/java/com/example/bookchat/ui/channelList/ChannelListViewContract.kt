package com.example.bookchat.ui.channelList

import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.data.networkmanager.external.model.NetworkState
import com.example.bookchat.ui.channelList.model.ChannelListItem

data class ChannelListUiState(
	val uiState: UiState,
	val channelListItem: List<ChannelListItem>,
	val networkState: NetworkState,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = ChannelListUiState(
			uiState = UiState.EMPTY,
			channelListItem = listOf(ChannelListItem.Header),
			networkState = NetworkState.DISCONNECTED,
		)
	}
}

sealed class ChannelListUiEvent {
	object MoveToMakeChannelPage : ChannelListUiEvent()
	object MoveToSearchChannelPage : ChannelListUiEvent()
	data class MoveToChannel(
		val channelId: Long,
	) : ChannelListUiEvent()

	data class ShowChannelSettingDialog(
		val clientAuthority: ChannelMemberAuthority,
		val channel: ChannelListItem.ChannelItem,
	) : ChannelListUiEvent()

	data class MakeToast(
		val stringId: Int,
	) : ChannelListUiEvent()
}