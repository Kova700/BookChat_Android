package com.example.bookchat.ui.bookreport

import com.example.bookchat.domain.model.BookReport
import com.example.bookchat.domain.model.BookShelfItem


data class BookReportUiState(
	val uiState: UiState,
	val bookshelfItem: BookShelfItem,
	val existingBookReport: BookReport,
	val enteredTitle: String,
	val enteredContent: String,
) {
	val isEditOrEmpty
		get() = uiState == UiState.EDITING
						|| uiState == UiState.EMPTY

	val isExistChanges
		get() = existingBookReport.reportTitle != enteredTitle
						|| existingBookReport.reportContent != enteredContent

	val isEnteredTextEmpty
		get() = enteredTitle.isEmpty()
						|| enteredContent.isEmpty()

	enum class UiState {
		SUCCESS,
		LOADING,
		EMPTY,
		EDITING,
		ERROR,
	}

	companion object {
		val DEFAULT = BookReportUiState(
			uiState = UiState.LOADING,
			bookshelfItem = BookShelfItem.DEFAULT,
			existingBookReport = BookReport.DEFAULT,
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