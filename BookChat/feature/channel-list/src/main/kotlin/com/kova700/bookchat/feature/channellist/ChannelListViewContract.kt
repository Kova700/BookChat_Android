package com.kova700.bookchat.feature.channellist

import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.feature.channellist.model.ChannelListItem

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
	data object MoveToMakeChannelPage : ChannelListUiEvent()
	data object MoveToSearchChannelPage : ChannelListUiEvent()
	data class MoveToChannel(
		val channelId: Long,
	) : ChannelListUiEvent()

	data class ShowChannelSettingDialog(
		val clientAuthority: ChannelMemberAuthority,
		val channel: ChannelListItem.ChannelItem,
	) : ChannelListUiEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : ChannelListUiEvent()
}