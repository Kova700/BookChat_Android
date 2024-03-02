package com.example.bookchat.ui.viewmodel.contract

import com.example.bookchat.data.Book
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.User

data class HomeUiState(
	val client: User,
	val bookUiState: UiState,
	val channelUiState: UiState,
	val readingBooks: List<Book>,
	val channels: List<Channel>,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = HomeUiState(
			client = User.Default,
			bookUiState = UiState.EMPTY,
			channelUiState = UiState.EMPTY,
			readingBooks = emptyList(),
			channels = emptyList()
		)
	}
}