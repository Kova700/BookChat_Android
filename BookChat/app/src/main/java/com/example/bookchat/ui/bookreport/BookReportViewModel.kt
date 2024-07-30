package com.example.bookchat.ui.bookreport

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.BookReportDoseNotExistException
import com.example.bookchat.domain.repository.BookReportRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookreport.BookReportUiState.UiState
import com.example.bookchat.ui.bookshelf.complete.dialog.CompleteBookDialog.Companion.EXTRA_BOOKREPORT_BOOKSHELF_ITEM_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 추후 Room 사용
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
	}

	private fun getBookShelfItem() = viewModelScope.launch {
		val item = bookShelfRepository.getCachedBookShelfItem(bookShelfItemId)
		item?.let { updateState { copy(bookshelfItem = item) } }
	}

	private fun getBookReport() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { bookReportRepository.getBookReport(bookShelfItemId) }
			.onSuccess { bookReport ->
				updateState {
					copy(
						uiState = UiState.SUCCESS,
						existingBookReport = bookReport
					)
				}
			}
			.onFailure { failHandler(it) }
	}

	//TODO : 장문인 경우 BadRequestException 에러 해결 필요
	private fun registerBookReport() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			bookReportRepository.registerBookReport(
				bookShelfId = bookShelfItemId,
				reportTitle = uiState.value.enteredTitle,
				reportContent = uiState.value.enteredContent,
				reportCreatedAt = uiState.value.existingBookReport.reportCreatedAt,
			)
		}
			.onSuccess { bookReport ->
				updateState {
					copy(
						uiState = UiState.SUCCESS,
						existingBookReport = bookReport
					)
				}
			}
			.onFailure {
				updateState { copy(uiState = UiState.EMPTY) }
				startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_make_fail))
			}
	}

	//TODO : 장문인 경우 BadRequestException 에러 해결 필요
	private fun reviseBookReport() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			bookReportRepository.reviseBookReport(
				bookShelfId = bookShelfItemId,
				reportTitle = uiState.value.enteredTitle,
				reportContent = uiState.value.enteredContent,
				reportCreatedAt = uiState.value.existingBookReport.reportCreatedAt,
			)
		}
			.onSuccess { bookReport ->
				updateState {
					copy(
						uiState = UiState.SUCCESS,
						existingBookReport = bookReport
					)
				}
			}
			.onFailure {
				updateState { copy(uiState = UiState.REVISE) }
				startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_revise_fail))
			}
	}

	private fun deleteBookReport() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { bookReportRepository.deleteBookReport(bookShelfItemId) }
			.onSuccess {
				startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_delete_success))
				startEvent(BookReportEvent.MoveBack)
			}
			.onFailure {
				updateState { copy(uiState = UiState.SUCCESS) }
				startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_delete_fail))
			}
	}

	fun onClickRegisterBtn() {
		if (isEnteredTextEmpty()) {
			startEvent(BookReportEvent.ShowSnackBar(R.string.title_content_empty))
			return
		}

		when (uiState.value.uiState) {
			UiState.EMPTY -> registerBookReport()
			UiState.REVISE -> {
				if (isNotChangedReport()) {
					updateState { copy(uiState = UiState.SUCCESS) }
					return
				}
				reviseBookReport()
			}

			else -> {}
		}
	}

	fun onChangeTitle(text: String) {
		updateState { copy(enteredTitle = text.trim()) }
	}

	fun onChangeContent(text: String) {
		updateState { copy(enteredContent = text.trim()) }
	}

	private fun isEnteredTextEmpty(): Boolean {
		return uiState.value.enteredTitle.isEmpty() ||
						uiState.value.enteredContent.isEmpty()
	}

	private fun isEditState(): Boolean {
		return uiState.value.uiState == UiState.EMPTY ||
						uiState.value.uiState == UiState.REVISE
	}

	private fun isNotChangedReport(): Boolean {
		return (uiState.value.enteredTitle == uiState.value.existingBookReport.reportTitle) &&
						(uiState.value.enteredContent == uiState.value.existingBookReport.reportContent)
	}

	fun onClickReviseBtn() {
		updateState {
			copy(
				uiState = UiState.REVISE,
				enteredTitle = existingBookReport.reportTitle,
				enteredContent = existingBookReport.reportContent,
			)
		}
	}

	fun onClickDeleteBtn() {
		startEvent(BookReportEvent.ShowDeleteWarningDialog(
			stringId = R.string.book_report_delete_warning,
			onOkClick = { deleteBookReport() }
		))
	}

	fun onClickBackBtn() {
		if (isEditState().not()) {
			startEvent(BookReportEvent.MoveBack)
			return
		}

		if (isNotChangedReport() || isEnteredTextEmpty()) {
			startEvent(BookReportEvent.MoveBack)
			return
		}

		startEvent(BookReportEvent.ShowDeleteWarningDialog(
			stringId = R.string.book_report_writing_cancel_warning,
			onOkClick = { startEvent(BookReportEvent.MoveBack) }
		))
	}

	private fun startEvent(event: BookReportEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: BookReportUiState.() -> BookReportUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun failHandler(exception: Throwable) {
		when (exception) {
			is BookReportDoseNotExistException -> updateState { copy(uiState = UiState.EMPTY) }
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(BookReportEvent.ErrorEvent(R.string.error_else))
				else startEvent(BookReportEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}
}