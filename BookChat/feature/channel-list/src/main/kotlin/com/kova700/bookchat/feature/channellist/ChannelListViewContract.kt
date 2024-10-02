package com.kova700.bookchat.feature.channellist

import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.feature.channellist.model.ChannelListItem

data class ChannelListUiState(
	val uiState: UiState,
	val channelListItem: List<ChannelListItem>,
	val networkState: NetworkState,
) {
	val isLoading
		get() = isInitLoading || isPageLoading

	val isPageLoading
		get() = uiState == UiState.PAGING_LOADING

	val isInitError
		get() = uiState == UiState.INIT_ERROR

	val isInitLoading
		get() = uiState == UiState.INIT_LOADING

	val isEmpty
		get() = channelListItem.isEmpty()
						&& isInitError.not()
						&& isInitLoading.not()

	enum class UiState {
		SUCCESS,
		INIT_ERROR,
		INIT_LOADING,
		PAGING_ERROR,
		PAGING_LOADING,
	}

	companion object {
		val DEFAULT = ChannelListUiState(
			uiState = UiState.SUCCESS,
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