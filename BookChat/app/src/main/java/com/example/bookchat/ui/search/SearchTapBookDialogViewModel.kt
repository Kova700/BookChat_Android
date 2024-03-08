package com.example.bookchat.ui.search

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.response.BookStateInBookShelfResponse
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.BookStateInBookShelf
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.utils.RefreshManager
import com.example.bookchat.utils.RefreshManager.BookShelfRefreshFlag
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SearchTapBookDialogViewModel @AssistedInject constructor(
	private val bookShelfRepository: BookShelfRepository,
	@Assisted val book: Book
) : ViewModel() {
	val stateFlow = MutableStateFlow<SearchTapDialogState>(SearchTapDialogState.Loading)
	var isToggleChecked = MutableStateFlow<Boolean>(false)

	private val _eventFlow = MutableSharedFlow<SearchTapDialogEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		if (!isNetworkConnected()) {
			makeToast(R.string.error_network)
		} else checkAlreadyInBookShelf()
	}

	fun requestToggleApi() = viewModelScope.launch {
		if (!isNetworkConnected()) {
			makeToast(R.string.error_network)
			return@launch
		}
		if (!isToggleChecked.value) {
			requestRegisterWishBook()
			return@launch
		}
	}

	private fun checkAlreadyInBookShelf() = viewModelScope.launch {
		runCatching { bookShelfRepository.checkAlreadyInBookShelf(book) }
			.onSuccess { setDialogStateFromRespond(it) }
	}

	private fun setDialogStateFromRespond(bookStateInBookShelf: BookStateInBookShelf?) {
		bookStateInBookShelf?.let {
			setDialogState(SearchTapDialogState.AlreadyInBookShelf(it.bookShelfState))
			if (isAlreadyInWishBookShelf(stateFlow.value)) isToggleChecked.value = true
			return
		}
		setDialogState(SearchTapDialogState.Default)
	}

	private fun requestRegisterWishBook() = viewModelScope.launch {
		runCatching {
			bookShelfRepository.registerBookShelfBook(
				book = book,
				bookShelfState = BookShelfState.WISH
			)
		}
			.onSuccess { registerWishBookSuccessCallBack() }
			.onFailure { makeToast(R.string.wish_bookshelf_register_fail) }
	}

	fun requestRegisterReadingBook() = viewModelScope.launch {
		runCatching {
			bookShelfRepository.registerBookShelfBook(
				book = book,
				bookShelfState = BookShelfState.READING
			)
		}
			.onSuccess { registerReadingBookSuccessCallBack() }
			.onFailure { makeToast(R.string.reading_bookshelf_register_fail) }
	}

	private fun registerWishBookSuccessCallBack() {
		makeToast(R.string.wish_bookshelf_register_success)
		RefreshManager.addBookShelfRefreshFlag(BookShelfRefreshFlag.Wish)
		isToggleChecked.value = true
		setDialogState(SearchTapDialogState.AlreadyInBookShelf(BookShelfState.WISH))
	}

	private fun registerReadingBookSuccessCallBack() {
		makeToast(R.string.reading_bookshelf_register_success)
		RefreshManager.addBookShelfRefreshFlag(BookShelfRefreshFlag.Reading)
		setDialogState(SearchTapDialogState.AlreadyInBookShelf(BookShelfState.READING))
	}

	fun setDialogState(state: SearchTapDialogState) {
		stateFlow.value = state
	}

	fun isDialogStateLoading(state: SearchTapDialogState) =
		state == SearchTapDialogState.Loading

	fun isDialogStateDefault(state: SearchTapDialogState) =
		state == SearchTapDialogState.Default

	fun isDialogStateAlreadyIn(state: SearchTapDialogState) =
		state is SearchTapDialogState.AlreadyInBookShelf

	fun isAlreadyInWishBookShelf(state: SearchTapDialogState) =
		state is SearchTapDialogState.AlreadyInBookShelf &&
						(state.bookShelfState == BookShelfState.WISH)

	fun clickCompleteBtn() {
		startUiEvent(SearchTapDialogEvent.OpenSetStarsDialog)
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	private fun makeToast(stringId: Int) {
		Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
	}

	private fun startUiEvent(event: SearchTapDialogEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	sealed class SearchTapDialogEvent {
		object OpenSetStarsDialog : SearchTapDialogEvent()
	}

	sealed class SearchTapDialogState {
		object Loading : SearchTapDialogState()
		object Default : SearchTapDialogState()
		data class AlreadyInBookShelf(val bookShelfState: BookShelfState) : SearchTapDialogState()
	}

	@dagger.assisted.AssistedFactory
	interface AssistedFactory {
		fun create(book: Book): SearchTapBookDialogViewModel
	}

	companion object {
		fun provideFactory(
			assistedFactory: AssistedFactory,
			book: Book
		): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return assistedFactory.create(book) as T
			}
		}
	}

}