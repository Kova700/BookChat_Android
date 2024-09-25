package com.kova700.bookchat.feature.bookreport

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.core.data.bookreport.external.model.BookReport

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