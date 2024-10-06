package com.kova700.bookchat.feature.bookreport

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.core.data.bookreport.external.model.BookReport

data class BookReportUiState(
	val uiState: UiState,
	val bookshelfItem: BookShelfItem,
	val existingBookReport: BookReport?,
	val enteredTitle: String,
	val enteredContent: String,
) {
	val isLoading
		get() = uiState == UiState.LOADING

	val isInitError
		get() = uiState == UiState.INIT_ERROR

	val isEditing
		get() = uiState == UiState.EDITING

	val isNotExistBookReport
		get() = existingBookReport == null
						&& isLoading.not()
						&& isInitError.not()

	val isExistChanges
		get() = isEditing
						&& (existingBookReport?.reportTitle != enteredTitle
						|| existingBookReport.reportContent != enteredContent)

	val isEnteredTextBlank
		get() = isEditing
						&& (enteredTitle.isBlank() || enteredContent.isBlank())

	enum class UiState {
		SUCCESS,
		LOADING,
		INIT_ERROR,
		EDITING,
	}

	companion object {
		val DEFAULT = BookReportUiState(
			uiState = UiState.SUCCESS,
			bookshelfItem = BookShelfItem.DEFAULT,
			existingBookReport = null,
			enteredTitle = "",
			enteredContent = "",
		)
	}
}

sealed class BookReportEvent {
	data object MoveBack : BookReportEvent()
	data class ShowDeleteWarningDialog(
		val stringId: Int,
		val onOkClick: () -> Unit,
	) : BookReportEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : BookReportEvent()

	data class UnknownErrorEvent(val message: String) : BookReportEvent()
}