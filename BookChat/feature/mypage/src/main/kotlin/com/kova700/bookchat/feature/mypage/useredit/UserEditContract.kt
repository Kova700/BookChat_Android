package com.kova700.bookchat.feature.mypage.useredit

import android.graphics.Bitmap
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.util.user.namecheck.NicknameCheckState

data class UserEditUiState(
	val uiState: UiState,
	val newNickname: String,
	val clientNewImage: Bitmap?,
	val nicknameCheckState: NicknameCheckState,
	val client: User,
	val isSelectedDefaultImage: Boolean,
) {

	val isExistsChange
		get() = isNicknameChanged
						|| isProfileChanged
						|| isSelectedDefaultImage

	private val isNicknameChanged
		get() = newNickname != client.nickname

	val isProfileChanged
		get() = (clientNewImage != null)
						|| isSelectedDefaultImage

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
			client = User.DEFAULT,
			isSelectedDefaultImage = false
		)
	}

}

sealed class UserEditUiEvent {
	data object MoveToGallery : UserEditUiEvent()
	data object MoveToBack : UserEditUiEvent()
	data object ShowProfileEditDialog : UserEditUiEvent()
	data object CloseKeyboard : UserEditUiEvent()
	data class ShowSnackBar(val stringId: Int) : UserEditUiEvent()
}