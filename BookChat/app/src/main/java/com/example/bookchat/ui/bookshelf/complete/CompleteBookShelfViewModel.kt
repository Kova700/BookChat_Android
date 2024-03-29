package com.example.bookchat.ui.bookshelf.complete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.bookshelf.complete.CompleteBookShelfUiState.UiState
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfItem
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : EmptyList 인 경우 UI 노출 + 도서 추가 하기 누르면 검색 Fragment로 이동
// TOOD : 도서 제거시 리스트에서 제거
// TOOD : 도서 삭제가 완료되었습니다 스낵바노출 (3초뒤 삭제 API 호출 예약)
// TODO : 실행 취소 버튼(호출 예약 취소 + 리스트에 다시 추가)
@HiltViewModel
class CompleteBookShelfViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<CompleteBookShelfEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState =
		MutableStateFlow<CompleteBookShelfUiState>(CompleteBookShelfUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private val _isSwiped = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

	init {
		observeBookShelfItems()
		getBookShelfItems()
	}

	private fun observeBookShelfItems() = viewModelScope.launch {
		bookShelfRepository.getBookShelfFlow(BookShelfState.COMPLETE)
			.combine(_isSwiped) { items, isSwipedMap ->
				items.map {
					it.toBookShelfListItem(isSwipedMap[it.bookShelfId] ?: false)
				}
			}.combine(
				bookShelfRepository.getBookShelfTotalItemCountFlow(BookShelfState.COMPLETE)
			) { items, totalCount -> groupCompleteItems(items, totalCount) }
			.collect { newItems -> updateState { copy(completeItems = newItems) } }
	}

	private fun groupCompleteItems(
		readingItems: List<BookShelfListItem>,
		totalItemCount: Int
	): List<CompleteBookShelfItem> {
		val groupedWishItems = mutableListOf<CompleteBookShelfItem>()
		groupedWishItems.add(CompleteBookShelfItem.Header(totalItemCount))
		groupedWishItems.addAll(readingItems.map { CompleteBookShelfItem.Item(it) })
		return groupedWishItems
	}

	private fun getBookShelfItems() =
		viewModelScope.launch {
			updateState { copy(uiState = UiState.LOADING) }
			runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.COMPLETE) }
				.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
				.onFailure { handleError(it) }
		}

	fun loadNextBookShelfItems(lastVisibleItemPosition: Int) {
		if (uiState.value.completeItems.size - 1 > lastVisibleItemPosition ||
			uiState.value.uiState == UiState.LOADING
		) return
		getBookShelfItems()
	}

	private fun deleteBookShelfItem(bookShelfListItem: BookShelfListItem) =
		viewModelScope.launch {
			runCatching {
				bookShelfRepository.deleteBookShelfBook(
					bookShelfListItem.bookShelfId,
					BookShelfState.COMPLETE
				)
			}.onFailure { makeToast(R.string.bookshelf_delete_fail) }
		}

	fun onItemClick(bookShelfListItem: BookShelfListItem) {
		startEvent(CompleteBookShelfEvent.MoveToCompleteBookDialog(bookShelfListItem))
	}

	fun onItemLongClick(bookShelfListItem: BookShelfListItem, isSwipe: Boolean) {
		_isSwiped.update { _isSwiped.value + (bookShelfListItem.bookShelfId to isSwipe) }
	}

	fun onItemDeleteClick(bookShelfListItem: BookShelfListItem) {
		deleteBookShelfItem(bookShelfListItem)
	}

	private inline fun updateState(block: CompleteBookShelfUiState.() -> CompleteBookShelfUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}

	private fun startEvent(event: CompleteBookShelfEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }
	}
}