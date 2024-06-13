package com.example.bookchat.ui.search.channelInfo

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.mapper.toChannel
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChannelSearchRepository
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_CLICKED_CHANNEL_ID
import com.example.bookchat.utils.Constants.TAG
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

	//TODO : 검색된 결과는 isEntered인 채로 있는데
	//  이 창을 가지고 채널 목록 가서 채널을 나간다음
	//  입장버튼을 누르면 원인 불명의 nullPointException이 나옴
	private fun enterChannel() = viewModelScope.launch {
		if (uiState.value.channel.isEntered) {
			startEvent(ChannelInfoEvent.MoveToChannel(uiState.value.channel.roomId))
			return@launch
		}

		if (uiState.value.channel.isFull) {
			startEvent(ChannelInfoEvent.ShowFullChannelDialog)
			return@launch
		}

		//TODO : 입장 요청한 채팅방 정원이 꽉찬 경우
		// 유저에게 정원이 꽉차서 입장에 실패했음을 알리기 위해
		// 정원이 꽉차서 실패한 경우에 대한 응답을 서버로 부터 받는게 필요
		// 클라이언트 단에서 1차적으로 막겠지만 2차적으로 서버에도 필요하다고 생각됨
		//  수정된다면 위에 다이얼로그 띄우는 로직 failHandler로 옮기기
		runCatching { channelRepository.enterChannel(uiState.value.channel.toChannel()) }
			.onSuccess { startEvent(ChannelInfoEvent.MoveToChannel(uiState.value.channel.roomId)) }
			.onFailure {
				failHandler(it)
				startEvent(ChannelInfoEvent.MakeToast(R.string.enter_chat_room_fail))
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

	private fun failHandler(throwable: Throwable) {
		Log.d(TAG, "ChannelInfoViewModel: failHandler() - throwable : $throwable")
	}
}