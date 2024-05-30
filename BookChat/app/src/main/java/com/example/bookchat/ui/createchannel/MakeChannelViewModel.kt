package com.example.bookchat.ui.createchannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.repository.BookSearchRepository
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.ui.createchannel.MakeChannelUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MakeChannelViewModel @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val bookSearchRepository: BookSearchRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<MakeChannelEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<MakeChannelUiState>(MakeChannelUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private fun makeChannel() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			channelRepository.makeChannel(
				channelTitle = uiState.value.channelTitle.trim(),
				channelSize = DEFAULT_ROOM_SIZE,
				defaultRoomImageType = uiState.value.defaultProfileImageType,
				channelTags = uiState.value.channelTagList,
				selectedBook = uiState.value.selectedBook!!,
				channelImage = uiState.value.channelProfileImage
			)
		}
			.onSuccess { channel ->
				updateState { copy(uiState = UiState.SUCCESS) }
				enterChannel(channel)
			}
			.onFailure { startEvent(MakeChannelEvent.MakeToast(R.string.make_chat_room_fail)) }
	}

	private fun enterChannel(channel: Channel) = viewModelScope.launch {
		runCatching { channelRepository.enterChannel(channel) }
			.onSuccess { startEvent(MakeChannelEvent.MoveToChannel(channel.roomId)) }
			.onFailure { startEvent(MakeChannelEvent.MakeToast(R.string.enter_chat_room_fail)) }
	}

	fun onClickFinishBtn() {
		if (uiState.value.isPossibleMakeChannel.not() ||
			uiState.value.uiState == UiState.LOADING
		) return

		makeChannel()
	}

	fun onChangeHashTag(text: String?) {
		text?.let { updateState { copy(channelTag = it) } }
	}

	fun onChangeChannelTitle(text: String?) {
		text?.let { updateState { copy(channelTitle = it) } }
	}

	fun onChangeChannelImg(byteArray: ByteArray) {
		updateState { copy(channelProfileImage = byteArray) }
	}

	fun onChangeSelectedBook(bookIsbn: String) {
		val book = bookSearchRepository.getCachedBook(bookIsbn)
		updateState { copy(selectedBook = book) }
	}

	fun onClickTextDeleteBtn() {
		updateState { copy(channelTitle = "") }
	}

	fun onClickXBtn() {
		startEvent(MakeChannelEvent.MoveToBack)
	}

	fun onClickSelectBookBtn() {
		startEvent(MakeChannelEvent.MoveToBookSelect)
	}

	fun onClickDeleteSelectedBookBtn() {
		updateState { copy(selectedBook = null) }
	}

	fun onClickImgEditBtn() {
		startEvent(MakeChannelEvent.OpenGallery)
	}

	private inline fun updateState(block: MakeChannelUiState.() -> MakeChannelUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: MakeChannelEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	companion object {
		private const val DEFAULT_ROOM_SIZE = 100
	}
}