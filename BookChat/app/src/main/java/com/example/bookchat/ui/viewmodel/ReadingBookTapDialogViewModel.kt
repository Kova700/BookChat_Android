package com.example.bookchat.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.domain.repository.BookRepository
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.toStarRating
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ReadingBookTapDialogViewModel @AssistedInject constructor(
	private val bookRepository: BookRepository,
	@Assisted val bookShelfDataItem: BookShelfDataItem
) : ViewModel() {
	private val _eventFlow = MutableSharedFlow<ReadingBookEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	var starRating = MutableStateFlow<Float>(0.0F)

	fun changeToCompleteBook() = viewModelScope.launch {
		val newItem = bookShelfDataItem.bookShelfItem.copy(star = starRating.value.toStarRating())
		runCatching { bookRepository.changeBookShelfBookStatus(newItem, ReadingStatus.COMPLETE) }
			.onSuccess {
				makeToast(R.string.bookshelf_change_to_complete_success)
				startEvent(ReadingBookEvent.MoveToCompleteBook)
			}
			.onFailure { makeToast(R.string.bookshelf_change_to_complete_fail) }
	}

	fun openAgonizeActivity() {
		startEvent(ReadingBookEvent.OpenAgonize)
	}

	private fun startEvent(event: ReadingBookEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun makeToast(stringId: Int) {
		Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
	}

	sealed class ReadingBookEvent {
		object MoveToCompleteBook : ReadingBookEvent()
		object OpenAgonize : ReadingBookEvent()
	}

	@dagger.assisted.AssistedFactory
	interface AssistedFactory {
		fun create(bookShelfDataItem: BookShelfDataItem): ReadingBookTapDialogViewModel
	}

	companion object {
		fun provideFactory(
			assistedFactory: AssistedFactory,
			bookShelfDataItem: BookShelfDataItem
		): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return assistedFactory.create(bookShelfDataItem) as T
			}
		}
	}
}