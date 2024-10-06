package com.kova700.bookchat.feature.home

import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.feature.home.model.HomeItem

data class HomeUiState(
	val client: User,
	val bookUiState: UiState,
	val channelUiState: UiState,
	val items: List<HomeItem>,
) {

	enum class UiState {
		SUCCESS,
		INIT_LOADING,
		INIT_ERROR,
	}

	companion object {
		val DEFAULT = HomeUiState(
			client = User.Default,
			bookUiState = UiState.INIT_LOADING,
			channelUiState = UiState.INIT_LOADING,
			items = emptyList()
		)
	}
}

sealed class HomeUiEvent {
	data object MoveToReadingBookShelf : HomeUiEvent()
	data class MoveToChannel(val channelId: Long) : HomeUiEvent()
	data class ShowSnackBar(val stringId: Int) : HomeUiEvent()
	data object MoveToSearch : HomeUiEvent()
	data object MoveToMakeChannel : HomeUiEvent()
}
