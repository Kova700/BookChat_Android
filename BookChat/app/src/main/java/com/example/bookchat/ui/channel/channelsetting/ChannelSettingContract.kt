package com.example.bookchat.ui.channel.channelsetting

import com.example.bookchat.domain.model.Channel

data class ChannelSettingUiState(
	val uiState: UiState,
	val channel: Channel,
	val newTitle: String,
	val newTags: String,
	val newCapacity: Int,
	val newProfileImage: ByteArray?,
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
	object MoveBack : ChannelSettingUiEvent()
	object ExitChannel : ChannelSettingUiEvent()
	object MoveToGallery : ChannelSettingUiEvent()
	object ShowProfileEditDialog : ChannelSettingUiEvent()
	object ShowChannelExitWarningDialog : ChannelSettingUiEvent()
	object ShowChannelCapacityDialog : ChannelSettingUiEvent()
	object MoveHostManage : ChannelSettingUiEvent()
	object MoveSubHostManage : ChannelSettingUiEvent()
	data class MakeToast(
		val stringId: Int,
	) : ChannelSettingUiEvent()
}