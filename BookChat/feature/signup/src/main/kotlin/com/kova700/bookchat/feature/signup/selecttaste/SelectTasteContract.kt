package com.kova700.bookchat.feature.signup.selecttaste

import com.kova700.bookchat.core.data.client.external.model.ReadingTaste

data class SelectTasteState(
	val uiState: UiState,
	val readingTastes: List<ReadingTaste>,
	val nickname: String,
	val userProfile: ByteArray?,
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
			userProfile = null
		)
	}
}

sealed class SelectTasteEvent {
	object MoveToMain : SelectTasteEvent()
	object MoveToBack : SelectTasteEvent()

	data class ErrorEvent(val stringId: Int) : SelectTasteEvent()
	data class UnknownErrorEvent(val message: String) : SelectTasteEvent()
}