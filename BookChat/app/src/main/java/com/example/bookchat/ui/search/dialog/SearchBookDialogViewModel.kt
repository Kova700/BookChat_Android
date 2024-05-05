package com.example.bookchat.ui.search.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.toStarRating
import com.example.bookchat.domain.repository.BookSearchRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCHED_BOOK_ITEM_ID
import com.example.bookchat.ui.search.dialog.SearchDialogUiState.SearchDialogState
import com.example.bookchat.ui.search.dialog.SearchDialogUiState.SearchDialogState.AlreadyInBookShelf
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchBookDialogViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookShelfRepository: BookShelfRepository,
	private val bookSearchRepository: BookSearchRepository
) : ViewModel() {
	private val bookIsbn = savedStateHandle.get<String>(EXTRA_SEARCHED_BOOK_ITEM_ID)!!

	private val _uiState = MutableStateFlow<SearchDialogUiState>(SearchDialogUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private val _eventFlow = MutableSharedFlow<SearchTapDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		initUiState()
		checkAlreadyInBookShelf()
	}

	private fun initUiState() {
		updateState {
			copy(
				uiState = SearchDialogState.Default,
				book = bookSearchRepository.getCachedBook(bookIsbn)
			)
		}
	}

	private fun checkAlreadyInBookShelf() = viewModelScope.launch {
		runCatching {
			bookShelfRepository.checkAlreadyInBookShelf(
				bookSearchRepository.getCachedBook(bookIsbn)
			)
		}.onSuccess {
			updateState { copy(uiState = AlreadyInBookShelf(it.bookShelfState)) }
		}
	}

	private fun registerWishBook() = viewModelScope.launch {
		runCatching {
			bookShelfRepository.registerBookShelfBook(
				book = uiState.value.book,
				bookShelfState = BookShelfState.WISH
			)
		}
			.onSuccess {
				makeToast(R.string.wish_bookshelf_register_success)
				updateState { copy(uiState = AlreadyInBookShelf(BookShelfState.WISH)) }
			}
			.onFailure { makeToast(R.string.wish_bookshelf_register_fail) }
	}

	private fun registerReadingBook() = viewModelScope.launch {
		runCatching {
			bookShelfRepository.registerBookShelfBook(
				book = uiState.value.book,
				bookShelfState = BookShelfState.READING
			)
		}
			.onSuccess {
				makeToast(R.string.reading_bookshelf_register_success)
				updateState { copy(uiState = AlreadyInBookShelf(BookShelfState.READING)) }
			}
			.onFailure { makeToast(R.string.reading_bookshelf_register_fail) }
	}

	private fun registerCompleteBook() = viewModelScope.launch {
		runCatching {
			bookShelfRepository.registerBookShelfBook(
				book = uiState.value.book,
				bookShelfState = BookShelfState.COMPLETE,
				starRating = uiState.value.starRating.toStarRating()
			)
		}
			.onSuccess {
				makeToast(R.string.complete_bookshelf_register_success)
				updateState { copy(uiState = AlreadyInBookShelf(BookShelfState.COMPLETE)) }
			}
			.onFailure { makeToast(R.string.complete_bookshelf_register_fail) }
	}

	private fun isAlreadyInWishBookShelf(state: SearchDialogState) =
		state is AlreadyInBookShelf && (state.bookShelfState == BookShelfState.WISH)

	fun onStarRatingChange(rating: Float) {
		updateState { copy(starRating = rating) }
	}

	fun onClickWishToggleBtn() {
		if (isAlreadyInWishBookShelf(uiState.value.uiState)) return
		registerWishBook()
	}

	fun onClickReadingBtn() {
		registerReadingBook()
	}

	fun onClickCompleteBtn() {
		startUiEvent(SearchTapDialogEvent.MoveToStarSetDialog)
	}

	fun onClickCompleteOkBtn() {
		if (uiState.value.starRating == 0F) {
			makeToast(R.string.complete_bookshelf_star_set_empty)
			return
		}
		registerCompleteBook()
	}

	private inline fun updateState(block: SearchDialogUiState.() -> SearchDialogUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startUiEvent(event: SearchTapDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}