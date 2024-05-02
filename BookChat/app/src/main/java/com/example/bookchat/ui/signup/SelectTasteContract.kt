package com.example.bookchat.ui.signup

import com.example.bookchat.domain.model.ReadingTaste

/**
	ByteArray의 equals, hashCode는 내용물이 같음을 검사하지 않음으로
	ByteArray의 equals,hashCode를 재정의하라는 경고가 나옴.
	ByteArray가 바뀌면 매번 다른 값으로 인식하기 때문에,
	굳이 equals,hashCode를 재정의 할 필요는 없음
 */

data class SelectTasteState(
	val uiState: UiState,
	val readingTastes: List<ReadingTaste>,
	val nickname: String,
	val userProfileImage: ByteArray?
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = SelectTasteState(
			uiState = UiState.SUCCESS,
			readingTastes = emptyList(),
			nickname = "",
			userProfileImage = null
		)
	}
}

sealed class SelectTasteEvent {
	object MoveToMain : SelectTasteEvent()
	object MoveToBack : SelectTasteEvent()

	data class ErrorEvent(val stringId: Int) : SelectTasteEvent()
	data class UnknownErrorEvent(val message: String) : SelectTasteEvent()
}