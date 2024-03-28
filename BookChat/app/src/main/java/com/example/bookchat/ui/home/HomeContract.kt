package com.example.bookchat.ui.home

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class HomeUiState(
	val client: User,
	val bookUiState: UiState,
	val channelUiState: UiState,
	val readingBookShelfBooks: List<BookShelfListItem>,
	val channels: List<Channel>,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = HomeUiState(
			client = User.Default,
			bookUiState = UiState.SUCCESS,
			channelUiState = UiState.SUCCESS,
			readingBookShelfBooks = emptyList(),
			channels = emptyList()
		)
	}
}

sealed class HomeUiEvent {
	data class MoveToChannel(val channelId: Long) : HomeUiEvent()
	data class MoveToReadingBookShelf(val bookShelfListItemId: Long) : HomeUiEvent()
}
