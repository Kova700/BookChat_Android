package com.example.bookchat.ui.viewmodel.contract

import com.example.bookchat.domain.model.Channel

data class ChannelListUiState(

	val channels :List<Channel>,
){
	companion object{
		val DEFAULT = ChannelListUiState(
			channels = emptyList()
		)
	}
}

sealed class ChannelListUiEvent {
	object MoveToMakeChannelPage : ChannelListUiEvent()
	object MoveToSearchChannelPage : ChannelListUiEvent()
}