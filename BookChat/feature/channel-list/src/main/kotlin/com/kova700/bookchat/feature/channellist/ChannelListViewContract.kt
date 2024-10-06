package com.kova700.bookchat.feature.channellist

import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.feature.channellist.model.ChannelListItem

data class ChannelListUiState(
	val uiState: UiState,
	val client: User,
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

	val isNotEmpty
		get() = channelListItem.isNotEmpty()

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
			client = User.Default,
			channelListItem = emptyList(),
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
		val isClientHost: Boolean,
		val channel: ChannelListItem.ChannelItem,
	) : ChannelListUiEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : ChannelListUiEvent()
}