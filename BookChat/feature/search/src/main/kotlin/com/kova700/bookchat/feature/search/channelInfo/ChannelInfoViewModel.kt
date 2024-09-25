package com.kova700.bookchat.feature.search.channelInfo

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.channel.external.model.ChannelIsExplodedException
import com.kova700.bookchat.core.data.channel.external.model.ChannelIsFullException
import com.kova700.bookchat.core.data.channel.external.model.UserIsBannedException
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.search.channel.external.ChannelSearchRepository
import com.kova700.bookchat.core.data.search.channel.external.mapper.toChannel
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.search.SearchFragment.Companion.EXTRA_CLICKED_CHANNEL_ID
import com.kova700.bookchat.util.Constants.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelInfoViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val channelSearchRepository: ChannelSearchRepository,
	private val channelRepository: ChannelRepository,
) : ViewModel() {
	private val channelId = savedStateHandle.get<Long>(EXTRA_CLICKED_CHANNEL_ID)!!

	private val _eventFlow = MutableSharedFlow<ChannelInfoEvent>()
	val eventFlow get() = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<ChannelInfoUiState>(ChannelInfoUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		updateState { copy(channel = channelSearchRepository.getCachedChannel(channelId)) }
	}

	private fun enterChannel() = viewModelScope.launch {
		runCatching { channelRepository.enterChannel(uiState.value.channel.toChannel()) }
			.onSuccess { startEvent(ChannelInfoEvent.MoveToChannel(uiState.value.channel.roomId)) }
			.onFailure {
				failHandler(
					throwable = it,
					default = { startEvent(ChannelInfoEvent.ShowSnackBar(R.string.enter_chat_room_fail)) }
				)
			}
	}

	fun onClickEnterBtn() {
		enterChannel()
	}

	fun onClickBackBtn() {
		startEvent(ChannelInfoEvent.MoveToBack)
	}

	private fun startEvent(event: ChannelInfoEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: ChannelInfoUiState.() -> ChannelInfoUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun failHandler(
		throwable: Throwable,
		default: (() -> Unit)? = null,
	) {
		Log.d(TAG, "ChannelInfoViewModel: failHandler() -  throwable :$throwable")
		when (throwable) {
			is ChannelIsFullException -> startEvent(ChannelInfoEvent.ShowFullChannelNoticeDialog)
			is ChannelIsExplodedException -> startEvent(ChannelInfoEvent.ShowExplodedChannelDialog)
			is UserIsBannedException -> updateState { copy(isBannedChannel = true) }
			else -> default?.invoke()
		}
	}
}