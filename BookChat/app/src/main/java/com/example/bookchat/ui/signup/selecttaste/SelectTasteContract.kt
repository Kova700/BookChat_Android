package com.example.bookchat.ui.signup.selecttaste

import com.example.bookchat.domain.model.ReadingTaste

data class SelectTasteState(
	val uiState: UiState,
	val readingTastes: List<ReadingTaste>,
	val nickname: String,
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
		)
	}
}

sealed class SelectTasteEvent {
	object MoveToMain : SelectTasteEvent()
	object MoveToBack : SelectTasteEvent()

	data class ErrorEvent(val stringId: Int) : SelectTasteEvent()
	data class UnknownErrorEvent(val message: String) : SelectTasteEvent()
}