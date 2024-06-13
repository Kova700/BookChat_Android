package com.example.bookchat.ui.search.channelInfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.mapper.toChannel
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
		_uiState.update {
			_uiState.value.block()
		}
	}

	private fun failHandler(throwable: Throwable) {
		// TODO : 이미 채팅방에 입장한 유저라면 채팅방 페이지로 이동
		//  + 이미 DB에 해당 채팅방 정보가 있을거임 (초기 로그인시에 다 가져오니까)
	}
}