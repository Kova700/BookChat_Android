package com.example.bookchat.ui.agony.agonyrecord

import com.example.bookchat.ui.agony.agonyrecord.model.AgonyRecordListItem

data class AgonyRecordeUiState(
	val uiState: UiState,
	val records: List<AgonyRecordListItem>,
	val isEditing: Boolean = false,
) {
	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = AgonyRecordeUiState(
			uiState = UiState.SUCCESS,
			records = emptyList(),
		)
	}
}

sealed class AgonyRecordEvent {
	data object MoveToBack : AgonyRecordEvent()
	data class MoveToAgonyTitleEdit(
		val agonyId: Long,
		val bookshelfItemId: Long,
	) : AgonyRecordEvent()

	data object ShowEditCancelWarning : AgonyRecordEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : AgonyRecordEvent()
}