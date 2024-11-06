package com.kova700.bookchat.feature.createchannel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.search.book.external.BookSearchRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.createchannel.MakeChannelUiState.UiState
import com.kova700.bookchat.util.image.bitmap.compressToByteArray
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
	private val bookSearchRepository: BookSearchRepository,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<MakeChannelEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<MakeChannelUiState>(MakeChannelUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		updateState {
			copy(
				defaultProfileImageType = ChannelDefaultImageType.getNewRandomType(defaultProfileImageType)
			)
		}
	}

	private fun makeChannel() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			channelRepository.makeChannel(
				channelTitle = uiState.value.channelTitle.trim(),
				channelSize = DEFAULT_ROOM_SIZE,
				channelDefaultImageType = uiState.value.defaultProfileImageType,
				channelTags = uiState.value.channelTagList,
				selectedBook = uiState.value.selectedBook!!,
				channelImage = uiState.value.channelProfileImage?.compressToByteArray()
			)
		}.onSuccess { channel -> enterChannel(channel) }
			.onFailure { startEvent(MakeChannelEvent.ShowSnackBar(R.string.make_chat_room_fail)) }
			.also { updateState { copy(uiState = UiState.SUCCESS) } }
	}

	private fun enterChannel(channel: Channel) = viewModelScope.launch {
		runCatching { channelRepository.enterChannel(channel) }
			.onSuccess { startEvent(MakeChannelEvent.MoveToChannel(channel.roomId)) }
			.onFailure { startEvent(MakeChannelEvent.ShowSnackBar(R.string.enter_chat_room_fail)) }
			.also { updateState { copy(uiState = UiState.SUCCESS) } }
	}

	fun onClickFinishBtn() {
		if (uiState.value.isPossibleMakeChannel.not()
			|| uiState.value.isLoading
		) return
		makeChannel()
	}

	fun onChangeHashTag(text: String?) {
		if (text.isNullOrBlank() || text.length > CHANNEL_TAG_MAX_LENGTH) return
		updateState { copy(channelTag = text) }
	}

	fun onChangeChannelTitle(text: String?) {
		if (text.isNullOrBlank() || text.length > CHANNEL_TITLE_MAX_LENGTH) return
		updateState { copy(channelTitle = text) }
	}

	fun onChangeChannelImg(bitmap: Bitmap) {
		updateState { copy(channelProfileImage = bitmap) }
	}

	fun onChangeSelectedBook(bookIsbn: String) {
		val book = bookSearchRepository.getCachedBook(bookIsbn)
		updateState { copy(selectedBook = book) }
	}

	fun onClickTextClearBtn() {
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

	fun onClickCameraBtn() {
		startEvent(MakeChannelEvent.ShowChannelImageSelectDialog)
	}

	fun onClickChangeDefaultImage() {
		updateState {
			copy(
				defaultProfileImageType = ChannelDefaultImageType.getNewRandomType(
					defaultProfileImageType
				),
				channelProfileImage = null
			)
		}
	}

	fun onClickGallery() {
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
		const val CHANNEL_TITLE_MAX_LENGTH = 30
		const val CHANNEL_TAG_MAX_LENGTH = 50
	}
}