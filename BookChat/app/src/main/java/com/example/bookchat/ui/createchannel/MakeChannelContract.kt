package com.example.bookchat.ui.createchannel

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.ChannelDefaultImageType

data class MakeChannelUiState(
	val uiState: UiState,
	val channelTitle: String,
	val channelTag: String,
	val channelProfileImage: ByteArray?,
	val defaultProfileImageType: ChannelDefaultImageType,
	val selectedBook: Book?
) {
	val channelTagList
		get() = channelTag.split(" ").filter { it.isNotBlank() }
			.map { it.split("#") }.flatten().filter { it.isNotBlank() }

	val isPossibleMakeChannel
		get() = channelTitle.trim().isNotBlank() &&
						channelTag.trim().isNotBlank() &&
						(selectedBook != null)

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = MakeChannelUiState(
			uiState = UiState.SUCCESS,
			channelTitle = "",
			channelTag = "",
			channelProfileImage = null,
			defaultProfileImageType = ChannelDefaultImageType.getNewRandomType(),
			selectedBook = null
		)
	}

}

sealed class MakeChannelEvent {

	object MoveToBack : MakeChannelEvent()
	object MoveToBookSelect : MakeChannelEvent()
	data class MoveToChannel(val channelId: Long) : MakeChannelEvent()
	object OpenGallery : MakeChannelEvent()
}
