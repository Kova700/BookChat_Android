package com.example.bookchat.ui.agony.agonyedit

import com.example.bookchat.domain.model.Agony

data class AgonyEditUiState(
	val uiState: UiState,
	val newTitle: String,
	val agony: Agony,
) {

	val isPossibleChangeAgony
		get() = (agony.title != newTitle)
						&& newTitle.isNotBlank()

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = AgonyEditUiState(
			uiState = UiState.SUCCESS,
			newTitle = "",
			agony = Agony.DEFAULT
		)
	}
}

sealed class AgonyEditUiEvent {
	data object MoveToBack : AgonyEditUiEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : AgonyEditUiEvent()

	data object CloseKeyboard : AgonyEditUiEvent()

}
