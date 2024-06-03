package com.example.bookchat.ui.channel.channelsetting

import com.example.bookchat.domain.model.Channel

data class ChannelSettingUiState(
	val uiState: UiState,
	val channel: Channel,
	val newTitle: String,
	val newTags: String,
	val newCapacity: Int,
	val newProfileImage: ByteArray?,
) {
	private val isExistsChange
		get() = isTitleChanged || isTagsChanged
						|| isProfileChanged || isCapacityChanged

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
			//TODO : 근데 요청 보낼때 바뀐애들만 보내야지 안바뀐 애들은 밑에 텅빈값으로 보내면 다 초기화되어버림
			//유저 프로필 변경도 확인해볼 것
			//고민 기록 제목 수정도 마찬가지
			newTitle = "",
			newTags = "",
			newCapacity = 0,
			newProfileImage = null,
		)
	}
}

sealed class ChannelSettingUiEvent {
	object MoveBack : ChannelSettingUiEvent()
	object PermissionCheck : ChannelSettingUiEvent()
	object ShowChannelExitWarningDialog : ChannelSettingUiEvent()
	object ShowChannelCapacityDialog : ChannelSettingUiEvent()
	object MoveHostManage : ChannelSettingUiEvent()
	object MoveSubHostManage : ChannelSettingUiEvent()
	data class MakeToast(
		val stringId: Int,
	) : ChannelSettingUiEvent()
}