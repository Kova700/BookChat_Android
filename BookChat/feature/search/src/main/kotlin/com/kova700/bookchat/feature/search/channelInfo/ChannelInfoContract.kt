package com.kova700.bookchat.feature.search.channelInfo

import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult

data class ChannelInfoUiState(
	val uiState: UiState,
	val channel: ChannelSearchResult,
	val isBannedChannel: Boolean,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = ChannelInfoUiState(
			uiState = UiState.LOADING,
			channel = ChannelSearchResult.DEFAULT,
			isBannedChannel = false
		)
	}
}

sealed class ChannelInfoEvent {
	data object MoveToBack : ChannelInfoEvent()
	data object ShowFullChannelNoticeDialog : ChannelInfoEvent()
	data object ShowExplodedChannelDialog : ChannelInfoEvent()
	data class MoveToChannel(
		val channelId: Long,
	) : ChannelInfoEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : ChannelInfoEvent()
}
