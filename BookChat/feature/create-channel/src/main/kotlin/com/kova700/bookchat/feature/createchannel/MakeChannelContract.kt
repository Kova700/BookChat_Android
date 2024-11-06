package com.kova700.bookchat.feature.createchannel

import android.graphics.Bitmap
import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.data.search.book.external.model.Book

data class MakeChannelUiState(
	val uiState: UiState,
	val channelTitle: String,
	val channelTag: String,
	val channelProfileImage: Bitmap?,
	val defaultProfileImageType: ChannelDefaultImageType,
	val selectedBook: Book?,
) {
	val isLoading
		get() = uiState == UiState.LOADING

	val channelTagList
		get() = channelTag.trim().split(" ").filter { it.isNotBlank() }
			.map { it.split("#") }.flatten().filter { it.isNotBlank() }

	val isPossibleMakeChannel
		get() = channelTitle.trim().isNotBlank() &&
						channelTag.trim().isNotBlank() &&
						(selectedBook != null)

	enum class UiState {
		SUCCESS,
		LOADING,
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
	data object MoveToBack : MakeChannelEvent()
	data object MoveToBookSelect : MakeChannelEvent()
	data class MoveToChannel(val channelId: Long) : MakeChannelEvent()
	data object OpenGallery : MakeChannelEvent()
	data object ShowChannelImageSelectDialog : MakeChannelEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : MakeChannelEvent()
}
