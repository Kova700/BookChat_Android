package com.example.bookchat.ui.bookreport

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.BookReportDoseNotExistException
import com.example.bookchat.domain.repository.BookReportRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookreport.BookReportUiState.UiState
import com.example.bookchat.ui.bookshelf.complete.dialog.CompleteBookDialog.Companion.EXTRA_BOOKREPORT_BOOKSHELF_ITEM_ID
import com.example.bookchat.utils.Constants.TAG
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
		observeBookReport()
	}

	private fun initUiState() {
		getBookShelfItem()
		getBookReport()
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
		runCatching { bookReportRepository.getBookReport(bookShelfItemId) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { failHandler(it) }
	}

	//TODO : 내용이 긴 경우에는 등록이 되나,
	// 제목이 긴 경우는 해결되지 않음
	// --> POST https://bookchat.link/v1/api/bookshelves/124/report h2
	// {"errorCode":"500","message":"예상치 못한 예외가 발생했습니다."}
	private fun registerBookReport() {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }
		viewModelScope.launch {
			runCatching {
				bookReportRepository.registerBookReport(
					bookShelfId = bookShelfItemId,
					reportTitle = uiState.value.enteredTitle,
					reportContent = uiState.value.enteredContent,
					reportCreatedAt = uiState.value.existingBookReport.reportCreatedAt,
				)
			}
				.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
				.onFailure {
					updateState { copy(uiState = UiState.EMPTY) }
					startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_make_fail))
				}
		}
	}

	//TODO : 장문인 경우 BadRequestException 에러 해결 필요 (이거 이슈 해결됨 테스트하고 삭제)
	// 수정하고 뒤로 돌아 나갔다오면 수정내용이 반영되지 않는 현상이 있음
	private fun reviseBookReport() {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }

		Log.d(TAG, "BookReportViewModel: reviseBookReport() - called")
		viewModelScope.launch {
			runCatching {
				bookReportRepository.reviseBookReport(
					bookShelfId = bookShelfItemId,
					reportTitle = uiState.value.enteredTitle,
					reportContent = uiState.value.enteredContent,
					reportCreatedAt = uiState.value.existingBookReport.reportCreatedAt,
				)
			}
				//수정 실패했는데 왜 성공 UI가 나오지..?
				.onSuccess {
					Log.d(TAG, "BookReportViewModel: reviseBookReport() - onSuccess")
					updateState { copy(uiState = UiState.SUCCESS) } }
				.onFailure {
					Log.d(TAG, "BookReportViewModel: reviseBookReport() - onFailure")
					updateState { copy(uiState = UiState.EDITING) }
					startEvent(BookReportEvent.ShowSnackBar(R.string.book_report_revise_fail))
				}
		}
	}

	private fun deleteBookReport() {
		if (uiState.value.uiState == UiState.LOADING) return
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
		if (uiState.value.isEnteredTextEmpty) {
			startEvent(BookReportEvent.ShowSnackBar(R.string.title_content_empty))
			return
		}

		when (uiState.value.uiState) {
			UiState.EMPTY -> registerBookReport()
			UiState.EDITING -> {
				if (uiState.value.isExistChanges.not()) {
					updateState { copy(uiState = UiState.SUCCESS) }
					return
				}
				reviseBookReport()
			}

			else -> Unit
		}
	}

	fun onChangeTitle(text: String) {
		updateState { copy(enteredTitle = text.trim()) }
	}

	fun onChangeContent(text: String) {
		updateState { copy(enteredContent = text.trim()) }
	}

	fun onClickReviseBtn() {
		updateState {
			copy(
				uiState = UiState.EDITING,
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
		when {
			uiState.value.isEditOrEmpty.not() -> startEvent(BookReportEvent.MoveBack)
			uiState.value.isExistChanges.not()
							|| uiState.value.isEnteredTextEmpty -> startEvent(BookReportEvent.MoveBack)

			else -> startEvent(BookReportEvent.ShowDeleteWarningDialog(
				stringId = R.string.book_report_writing_cancel_warning,
				onOkClick = { startEvent(BookReportEvent.MoveBack) }
			))
		}
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
				if (errorMessage.isNullOrBlank()) startEvent(BookReportEvent.ShowSnackBar(R.string.error_else))
				else startEvent(BookReportEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}
}