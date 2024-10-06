package com.kova700.bookchat.feature.bookreport

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookreport.BookReportActivity.Companion.EXTRA_BOOKREPORT_BOOKSHELF_ITEM_ID
import com.kova700.bookchat.feature.bookreport.BookReportUiState.UiState
import com.kova700.core.data.bookreport.external.BookReportRepository
import com.kova700.core.data.bookreport.external.model.BookReportDoseNotExistException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookReportViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookShelfRepository: BookShelfRepository,
	private val bookReportRepository: BookReportRepository,
) : ViewModel() {
	private val bookShelfItemId = savedStateHandle.get<Long>(EXTRA_BOOKREPORT_BOOKSHELF_ITEM_ID)!!

	private val _uiState = MutableStateFlow<BookReportUiState>(BookReportUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private val _eventFlow = MutableSharedFlow<BookReportEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		getBookShelfItem()
		getBookReport()
		observeBookReport()
	}

	private fun observeBookReport() = viewModelScope.launch {
		bookReportRepository.getBookReportFlow(bookShelfItemId).collect {
			updateState { copy(existingBookReport = it) }
		}
	}

	private fun getBookShelfItem() = viewModelScope.launch {
		val item = bookShelfRepository.getCachedBookShelfItem(bookShelfItemId)
		updateState { copy(bookshelfItem = item) }
	}

	private fun getBookReport() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { bookReportRepository.getBookReport(bookShelfItemId) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { throwable ->
				if (throwable is BookReportDoseNotExistException) updateState { copy(uiState = UiState.EDITING) }
				else updateState { copy(uiState = UiState.INIT_ERROR) }
			}
	}

	private fun registerBookReport() {
		if (uiState.value.isLoading) return
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching {
				bookReportRepository.registerBookReport(
					bookShelfId = bookShelfItemId,
					reportTitle = uiState.value.enteredTitle,
					reportContent = uiState.value.enteredContent,
				)
			}
				.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
				.onFailure {
					updateState { copy(uiState = UiState.EDITING) }
					startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_make_fail))
				}
		}
	}

	private fun reviseBookReport() {
		if (uiState.value.isLoading) return
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching {
				bookReportRepository.reviseBookReport(
					bookShelfId = bookShelfItemId,
					reportTitle = uiState.value.enteredTitle,
					reportContent = uiState.value.enteredContent,
					reportCreatedAt = uiState.value.existingBookReport?.reportCreatedAt ?: "",
				)
			}.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
				.onFailure {
					updateState { copy(uiState = UiState.EDITING) }
					startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_revise_fail))
				}
		}
	}

	private fun deleteBookReport() {
		if (uiState.value.isLoading) return
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching { bookReportRepository.deleteBookReport(bookShelfItemId) }
				.onSuccess { startEvent(BookReportEvent.MoveBack) }
				.onFailure {
					updateState { copy(uiState = UiState.SUCCESS) }
					startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_delete_fail))
				}
		}
	}

	fun onClickRegisterBtn() {
		when {
			uiState.value.isEditing.not() -> return
			uiState.value.isEnteredTextBlank ->
				startEvent(BookReportEvent.ShowSnackBar(R.string.title_content_empty))

			uiState.value.isNotExistBookReport -> registerBookReport()
			uiState.value.isExistChanges.not() -> updateState { copy(uiState = UiState.SUCCESS) }
			else -> reviseBookReport()
		}
	}

	fun onChangeTitle(text: String) {
		if (text.length > BOOKREPORT_TITLE_MAX_LENGTH) return
		updateState { copy(enteredTitle = text.trim()) }
	}

	fun onChangeContent(text: String) {
		if (text.length > BOOKREPORT_CONTENT_MAX_LENGTH) return
		updateState { copy(enteredContent = text.trim()) }
	}

	fun onClickReviseBtn() {
		updateState {
			copy(
				uiState = UiState.EDITING,
				enteredTitle = existingBookReport?.reportTitle ?: "",
				enteredContent = existingBookReport?.reportContent ?: "",
			)
		}
	}

	fun onClickDeleteBtn() {
		startEvent(BookReportEvent.ShowDeleteWarningDialog(
			stringId = R.string.book_report_delete_warning,
			onOkClick = { deleteBookReport() }
		))
	}

	fun onClickRetryBtn() {
		getBookReport()
	}

	fun onClickBackBtn() {
		when {
			uiState.value.isEditing -> startEvent(BookReportEvent.ShowDeleteWarningDialog(
				stringId = R.string.book_report_writing_cancel_warning,
				onOkClick = { startEvent(BookReportEvent.MoveBack) }
			))

			else -> startEvent(BookReportEvent.MoveBack)
		}
	}

	private fun startEvent(event: BookReportEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: BookReportUiState.() -> BookReportUiState) {
		_uiState.update { _uiState.value.block() }
	}

	companion object {
		const val BOOKREPORT_TITLE_MAX_LENGTH = 500
		const val BOOKREPORT_CONTENT_MAX_LENGTH = 50000
	}
}