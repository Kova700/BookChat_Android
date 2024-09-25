package com.kova700.bookchat.feature.channel.channelsetting

import android.graphics.Bitmap
import com.kova700.bookchat.core.data.channel.external.model.Channel

data class ChannelSettingUiState(
	val uiState: UiState,
	val channel: Channel,
	val newTitle: String,
	val newTags: String,
	val newCapacity: Int,
	val newProfileImage: Bitmap?,
	val isSelectedDefaultImage: Boolean,
) {
	private val isExistsChange
		get() = isTitleChanged || isTagsChanged
						|| isProfileChanged || isCapacityChanged
						|| isSelectedDefaultImage

	private val isTitleChanged
		get() = newTitle != channel.roomName

	private val isTagsChanged
		get() = newTags != channel.tagsString

	private val isProfileChanged
		get() = newProfileImage != null

	private val isCapacityChanged
		get() = newCapacity != channel.roomCapacity

	val isPossibleChangeChannel
		get() = isExistsChange
						&& newTitle.trim().isNotBlank()
						&& newTags.trim().isNotBlank()

	val tagList
		get() = newTags.trim().split(" ").filter { it.isNotBlank() }
			.map { it.split("#") }.flatten().filter { it.isNotBlank() }

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = ChannelSettingUiState(
			uiState = UiState.SUCCESS,
			channel = Channel.DEFAULT,
			newTitle = "",
			newTags = "",
			newCapacity = 0,
			newProfileImage = null,
			isSelectedDefaultImage = false
		)
	}
}

sealed class ChannelSettingUiEvent {
	data object MoveBack : ChannelSettingUiEvent()
	data object ExitChannel : ChannelSettingUiEvent()
	data object MoveToGallery : ChannelSettingUiEvent()
	data object ShowProfileEditDialog : ChannelSettingUiEvent()
	data object ShowChannelExitWarningDialog : ChannelSettingUiEvent()
	data object ShowChannelCapacityDialog : ChannelSettingUiEvent()
	data object MoveHostManage : ChannelSettingUiEvent()
	data object MoveSubHostManage : ChannelSettingUiEvent()
	data object CloseKeyboard : ChannelSettingUiEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : ChannelSettingUiEvent()
}