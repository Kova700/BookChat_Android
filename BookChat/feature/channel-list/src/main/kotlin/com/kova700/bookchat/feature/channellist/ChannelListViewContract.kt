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

	val isNotEmpty
		get() = channelListItem.isNotEmpty()

	// 내가 하고 싶은게 서버로부터 채널 목록을 가져오는동안은 오프라인 데이터라도 보여주자는 의도가 큰데
	// INIT_LOADING과 INIT_ERROR일때, Rcv를 보여주지 않는다면 오프라인데이터를 사용하는 의미가 없어져버림
	// RCV는 로딩과, 에러유무와 상관없이 보여줘야함
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
		val clientAuthority: ChannelMemberAuthority,
		val channel: ChannelListItem.ChannelItem,
	) : ChannelListUiEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : ChannelListUiEvent()
}