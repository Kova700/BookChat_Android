package com.example.bookchat.ui.search.channelInfo

import com.example.bookchat.domain.model.ChannelSearchResult

data class ChannelInfoUiState(
	val uiState: UiState,
	val channel: ChannelSearchResult,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = ChannelInfoUiState(
			uiState = UiState.LOADING,
			channel = ChannelSearchResult.DEFAULT
		)
	}
}

sealed class ChannelInfoEvent {
	object MoveToBack : ChannelInfoEvent()
	object ShowFullChannelDialog : ChannelInfoEvent()
	data class MoveToChannel(
		val channelId: Long,
	) : ChannelInfoEvent()

	data class MakeToast(
		val stringId: Int,
	) : ChannelInfoEvent()
}
