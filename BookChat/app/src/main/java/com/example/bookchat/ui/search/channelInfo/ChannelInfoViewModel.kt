package com.example.bookchat.ui.search.channelInfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChannelSearchRepository
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_CLICKED_CHANNEL_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO : DB에 채팅방 있는거 보고 있으면 요청 안보내고 바로 채팅방 페이지 이동하는 걸로 수정
//  + DB에 채팅방이 없더라도, 서버로부터 Status Code 넘겨 받아서, 차단된 사용자인지,
//  채팅방 인원이 다 차서 못들어가는지, 이미 들어와있는 유저인지 ,
//  성공적으로 입장했는지, 분기가 필요함

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

	//TODO : 이미 입장되어있는 채팅방이더라도 200이 넘어와서 따로, 예외처리 하지 않았음
	private fun enterChannel() = viewModelScope.launch {
		runCatching { channelRepository.enter(uiState.value.channel) }
			.onSuccess { startEvent(ChannelInfoEvent.MoveToChannel(uiState.value.channel.roomId)) }
			.onFailure {
				failHandler(it)
				startEvent(ChannelInfoEvent.MakeToast(R.string.enter_chat_room_fail))
			}
	}

	fun onClickEnterBtn() = viewModelScope.launch {
		if (channelRepository.isAlreadyEntered(uiState.value.channel.roomId).not()) {
			enterChannel()
			return@launch
		}
		startEvent(ChannelInfoEvent.MoveToChannel(uiState.value.channel.roomId))
	}

	fun onClickBackBtn() {
		startEvent(ChannelInfoEvent.MoveToBack)
	}

	private fun startEvent(event: ChannelInfoEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: ChannelInfoUiState.() -> ChannelInfoUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}

	private fun failHandler(throwable: Throwable) {
		// TODO : 이미 채팅방에 입장한 유저라면 채팅방 페이지로 이동
		//  + 이미 DB에 해당 채팅방 정보가 있을거임 (초기 로그인시에 다 가져오니까)
	}
}