package com.example.bookchat.ui.mypage.useredit

import com.example.bookchat.domain.model.NicknameCheckState
import com.example.bookchat.domain.model.User

data class UserEditUiState(
	val uiState: UiState,
	val newNickname: String,
	val clientNewImage: ByteArray?,
	val nicknameCheckState: NicknameCheckState,
	val client: User,
) {

	val isExistsChange
		get() = isNicknameChanged || isProfileChanged

	private val isNicknameChanged
		get() = newNickname != client.nickname

	private val isProfileChanged
		get() = clientNewImage != null

	val isNeedDuplicatesNicknameCheck
		get() = isNicknameChanged
						&& nicknameCheckState != NicknameCheckState.IsPerfect

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = UserEditUiState(
			uiState = UiState.SUCCESS,
			newNickname = "",
			clientNewImage = null,
			nicknameCheckState = NicknameCheckState.Default,
			client = User.Default
		)
	}

}

sealed class UserEditUiEvent {
	object PermissionCheck : UserEditUiEvent()
	object MoveToBack : UserEditUiEvent()
	data class ErrorEvent(val stringId: Int) : UserEditUiEvent()
	data class UnknownErrorEvent(val message: String) : UserEditUiEvent()
}