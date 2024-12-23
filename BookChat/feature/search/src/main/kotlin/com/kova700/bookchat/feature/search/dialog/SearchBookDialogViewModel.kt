package com.kova700.bookchat.feature.search.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import com.kova700.bookchat.core.data.bookshelf.external.model.toStarRating
import com.kova700.bookchat.core.data.search.book.external.BookSearchRepository
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.search.SearchFragment.Companion.EXTRA_SEARCHED_BOOK_ITEM_ID
import com.kova700.bookchat.feature.search.dialog.SearchDialogUiState.SearchDialogState
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchTarget
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
	private val bookSearchRepository: BookSearchRepository,
) : ViewModel() {
	private val bookIsbn = savedStateHandle.get<String>(EXTRA_SEARCHED_BOOK_ITEM_ID)!!

	private val _uiState = MutableStateFlow<SearchDialogUiState>(SearchDialogUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private val _eventFlow = MutableSharedFlow<SearchTapDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		updateState { copy(book = bookSearchRepository.getCachedBook(bookIsbn)) }
		checkAlreadyInBookShelf()
	}

	private fun checkAlreadyInBookShelf() = viewModelScope.launch {
		updateState { copy(uiState = SearchDialogState.Loading) }
		runCatching { bookShelfRepository.checkAlreadyInBookShelf(uiState.value.book) }
			.onSuccess { updateState { copy(uiState = SearchDialogState.Success(it?.bookShelfState)) } }
			.onFailure {
				updateState { copy(uiState = SearchDialogState.InitError) }
				startEvent(SearchTapDialogEvent.ShowSnackBar(R.string.error_else_2_line))
			}
	}

	private fun registerBookshelf(
		bookShelfState: BookShelfState,
		starRating: StarRating? = null,
	) = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = SearchDialogState.Loading) }

		runCatching {
			bookShelfRepository.registerBookShelfBook(
				book = uiState.value.book,
				bookShelfState = bookShelfState,
				starRating = starRating
			)
		}
			.onSuccess {
				startEvent(SearchTapDialogEvent.ShowSnackBar(R.string.bookshelf_register_success))
				updateState { copy(uiState = SearchDialogState.Success(bookShelfState)) }
			}
			.onFailure {
				startEvent(SearchTapDialogEvent.ShowSnackBar(R.string.bookshelf_register_fail))
				updateState { copy(uiState = SearchDialogState.Success(null)) }
			}
	}

	fun onChangeStarRating(rating: Float) {
		updateState { copy(starRating = rating) }
	}

	fun onClickWishToggleBtn() {
		if (uiState.value.isAlreadyInBookShelf) return
		registerBookshelf(BookShelfState.WISH)
	}

	fun onClickReadingBtn() {
		registerBookshelf(BookShelfState.READING)
	}

	fun onClickCompleteBtn() {
		startEvent(SearchTapDialogEvent.MoveToStarSetDialog)
	}

	fun onClickCompleteOkBtn() {
		if (uiState.value.starRating == 0F) {
			startEvent(SearchTapDialogEvent.ShowSnackBar(R.string.complete_bookshelf_star_set_empty))
			return
		}
		registerBookshelf(
			bookShelfState = BookShelfState.COMPLETE,
			starRating = uiState.value.starRating.toStarRating()
		)
	}

	fun onClickChatBtn() {
		startEvent(
			SearchTapDialogEvent.MoveToChannelSearchWithSelectedBook(
				searchKeyword = uiState.value.book.isbn,
				searchTarget = SearchTarget.CHANNEL,
				searchPurpose = SearchPurpose.SEARCH_CHANNEL,
				searchFilter = SearchFilter.BOOK_ISBN
			)
		)
	}

	private inline fun updateState(block: SearchDialogUiState.() -> SearchDialogUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: SearchTapDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}