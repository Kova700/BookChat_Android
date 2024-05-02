package com.example.bookchat.ui.signup

import com.example.bookchat.domain.model.NameCheckStatus

/**
	ByteArray의 equals, hashCode는 내용물이 같음을 검사하지 않음으로
	ByteArray의 equals,hashCode를 재정의하라는 경고가 나옴.
	ByteArray가 바뀌면 매번 다른 값으로 인식하기 때문에,
	굳이 equals,hashCode를 재정의 할 필요는 없음
 */

data class SignUpState(
	val uiState: UiState,
	val nickname: String,
	val userProfileImage: ByteArray?,
	val nameCheckStatus: NameCheckStatus,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = SignUpState(
			uiState = UiState.SUCCESS,
			nickname = "",
			userProfileImage = null,
			nameCheckStatus = NameCheckStatus.Default,
		)
	}
}


sealed class SignUpEvent {
	data class MoveToSelectTaste(
		val userNickname: String,
		val userProfilByteArray: ByteArray?
	) : SignUpEvent()

	object PermissionCheck : SignUpEvent()
	object MoveToBack : SignUpEvent()

	data class ErrorEvent(val stringId: Int) : SignUpEvent()
	data class UnknownErrorEvent(val message: String) : SignUpEvent()
}
