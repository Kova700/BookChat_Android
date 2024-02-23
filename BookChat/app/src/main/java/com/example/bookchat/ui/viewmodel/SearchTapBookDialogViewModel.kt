package com.example.bookchat.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.domain.repository.BookRepository
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.RefreshManager
import com.example.bookchat.utils.RefreshManager.BookShelfRefreshFlag
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SearchTapBookDialogViewModel @AssistedInject constructor(
	private val bookRepository: BookRepository,
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
		runCatching { bookRepository.checkAlreadyInBookShelf(book) }
			.onSuccess { setDialogStateFromRespond(it) }
	}

	private fun setDialogStateFromRespond(respond: RespondCheckInBookShelf?) {
		respond?.let {
			setDialogState(SearchTapDialogState.AlreadyInBookShelf(it.readingStatus))
			if (isAlreadyInWishBookShelf(stateFlow.value)) isToggleChecked.value = true
			return
		}
		setDialogState(SearchTapDialogState.Default)
	}

	private fun requestRegisterWishBook() = viewModelScope.launch {
		val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.WISH)
		runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
			.onSuccess { registerWishBookSuccessCallBack() }
			.onFailure { makeToast(R.string.wish_bookshelf_register_fail) }
	}

	fun requestRegisterReadingBook() = viewModelScope.launch {
		val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.READING)
		runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
			.onSuccess { registerReadingBookSuccessCallBack() }
			.onFailure { makeToast(R.string.reading_bookshelf_register_fail) }
	}

	private fun registerWishBookSuccessCallBack() {
		makeToast(R.string.wish_bookshelf_register_success)
		RefreshManager.addBookShelfRefreshFlag(BookShelfRefreshFlag.Wish)
		isToggleChecked.value = true
		setDialogState(SearchTapDialogState.AlreadyInBookShelf(ReadingStatus.WISH))
	}

	private fun registerReadingBookSuccessCallBack() {
		makeToast(R.string.reading_bookshelf_register_success)
		RefreshManager.addBookShelfRefreshFlag(BookShelfRefreshFlag.Reading)
		setDialogState(SearchTapDialogState.AlreadyInBookShelf(ReadingStatus.READING))
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
						(state.readingStatus == ReadingStatus.WISH)

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
		data class AlreadyInBookShelf(val readingStatus: ReadingStatus) : SearchTapDialogState()
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