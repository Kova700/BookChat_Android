package com.kova700.bookchat.feature.agony.agonyrecord

import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem

data class AgonyRecordUiState(
	val uiState: UiState,
	val records: List<AgonyRecordListItem>,
) {
	val isNotInitErrorOrLoading: Boolean
		get() = (isInitLoading || isInitError).not()

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isPagingLoading: Boolean
		get() = uiState == UiState.PAGING_LOADING

	val isLoading: Boolean
		get() = isInitLoading || isPagingLoading

	val isInitError: Boolean
		get() = uiState == UiState.INIT_ERROR

	val isPagingError: Boolean
		get() = uiState == UiState.PAGING_ERROR

	val isEditing: Boolean
		get() = uiState == UiState.EDITING

	enum class UiState {
		SUCCESS,
		EDITING,
		INIT_LOADING,
		INIT_ERROR,
		PAGING_ERROR,
		PAGING_LOADING,
	}

	companion object {
		val DEFAULT = AgonyRecordUiState(
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