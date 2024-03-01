package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.database.dao.TempMessageDAO
import com.example.bookchat.data.repository.ChattingRepositoryFacade
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.StompHandler
import com.example.bookchat.ui.fragment.ChannelListFragment.Companion.EXTRA_CHAT_ROOM_ID
import com.example.bookchat.ui.viewmodel.contract.ChannelEvent
import com.example.bookchat.ui.viewmodel.contract.ChannelUiState
import com.example.bookchat.ui.viewmodel.contract.ChannelUiState.UiState
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

//TODO : 장문의 긴 채팅 길이 접기 구현해야함, 누르면 전체보기 가능하게
//TODO : 채팅 꾹 누르면 복사
//TODO : 뒤로가기 누를 때, 혹시 채팅방 메뉴 켜져 있으면 닫고, 화면 안꺼지게 수정
//TODO : 채팅방에 들어왔을때, 채팅스크롤의 위치는 내가 마지막으로 받았던 채팅 (여기까지 읽었습니다)
//TODO : 전송 중 상태로 어플 종료 시 전송실패로 변경된 UI로 보이게 수정
//TODO : 또한 전송 중 상태에서 인터넷이 끊겨도, 다시 인터넷이 연결되면 자동으로 전송이 되어야 함
//TODO : 전송 대기 중이던 채팅 소켓 연결 끊길 시, 혹은 특정 시간 지나면, 전송 실패 UI로 전환
//TODO : 소켓 재 연결 시마다, 채팅방 정보 조회 API 호출해서 수정 사항 전부 덮어쓰기
//TODO : 소켓 재 연결 시마다, 로컬에 저장된 마지막 채팅부터 서버의 마지막 채팅(isLast가 올 때)까지 페이징 요청
//TODO : 소켓 헤더에서 토큰 빼기 Or 특정 시간 주기로 토큰 갱신 API 호출 Or 소켓 끊길 때마다 리커넥션 요청으로 토큰 갱신 (백엔드와 협의)
//TODO : 소켓 연결 실패 시 예외 처리
//TODO : 채팅방 정보 조회 실패 시 예외 처리
//TODO : 채널 LastChat 이후 부터 채팅 페이징 요청
//TODO : 소켓 연결 성공 /실패 상관 없이 끝나면 채팅방 채팅 내역 조회

@HiltViewModel
class ChannelViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val tempMessageDAO: TempMessageDAO, //개선 필요
	private val stompHandler: StompHandler,
	private val chattingRepositoryFacade: ChattingRepositoryFacade,
) : ViewModel() {
	private val channelId = savedStateHandle.get<Long>(EXTRA_CHAT_ROOM_ID)!!

	private val _eventFlow = MutableSharedFlow<ChannelEvent>()
	val eventFlow get() = _eventFlow

	private val _uiStateFlow = MutableStateFlow<ChannelUiState>(ChannelUiState.DEFAULT)
	val uiStateFlow = _uiStateFlow.asStateFlow()

	val inputtedMessage = MutableStateFlow("")

	//아래 안보고 있을 때, 채팅들어오면 데이터 마지막 채팅 Notice 보여주는 방식으로 수정
	var newChatNoticeFlow = MutableStateFlow<Chat?>(null)
	var isFirstItemOnScreen = true
	var scrollForcedFlag = false

	init {
		observeChannel()
		getChannel(channelId)
		observeChats()
		getChats(channelId)
		getTempSavedMessage()
		clearTempSavedMessage()
		connectSocket()
	}

	private fun observeChannel() = viewModelScope.launch {
		chattingRepositoryFacade.getChannelFlow(channelId).collect { channel ->
			updateState { copy(channel = channel) }
		}
	}

	private fun observeChats() = viewModelScope.launch {
		chattingRepositoryFacade.getChatFlow().collect { chats ->
			updateState { copy(chats = chats) }
			if (isFirstItemOnScreen) return@collect
			newChatNoticeFlow.update { chats.first() }
		}
	}

	private fun getChats(channelId: Long) = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { chattingRepositoryFacade.getChats(channelId) }
			.onSuccess {
				updateState { copy(uiState = UiState.SUCCESS) }
			}
			.onFailure {
				handleError(it)
				updateState { copy(uiState = UiState.ERROR) }
			}
	}

	private fun getChannel(channelId: Long) = viewModelScope.launch {
		runCatching { chattingRepositoryFacade.getChannel(channelId) }
			.onSuccess {
				updateState { copy(uiState = UiState.SUCCESS) }
			}
			.onFailure {
				handleError(it)
				updateState { copy(uiState = UiState.ERROR) }
			}
	}

	fun loadNextChats(lastVisibleItemPosition: Int) {
		if (uiStateFlow.value.chats.size - 1 > lastVisibleItemPosition ||
			uiStateFlow.value.uiState == UiState.LOADING
		) return
		getChats(channelId)
	}

	private fun getTempSavedMessage() = viewModelScope.launch {
		inputtedMessage.value = tempMessageDAO.getTempMessage(channelId)?.message ?: ""
	}

	fun saveTempSavedMessage() = viewModelScope.launch {
		val tempMessage = inputtedMessage.value
		if (tempMessage.isBlank()) return@launch
		tempMessageDAO.insertOrUpdateTempMessage(channelId, tempMessage)
	}

	private fun clearTempSavedMessage() = viewModelScope.launch {
		tempMessageDAO.setTempSavedMessage(channelId, "")
	}

	private fun connectSocket() = viewModelScope.launch(Dispatchers.IO) {
		stompHandler.connectSocket(
			channelSId = uiStateFlow.value.channel?.roomSid ?: return@launch,
			channelId = channelId
		).catch { handleError(it) }.collect()
	}

	fun sendMessage() {
		if (inputtedMessage.value.isBlank()) return

		val message = inputtedMessage.value
		viewModelScope.launch(Dispatchers.IO) {
			inputtedMessage.update { "" }
			scrollForcedFlag = true
			runCatching { stompHandler.sendMessage(channelId, message) }
				.onFailure { handleError(it) }
		}
	}

	// TODO : Distroy 될 때도 소켓 닫게 수정 (자동으로 보내지는지 확인)
	fun finishActivity() {
		disconnectSocket()
		startEvent(ChannelEvent.MoveBack)
	}

	private fun disconnectSocket() = viewModelScope.launch {
		runCatching { stompHandler.disconnectSocket() }
			.onFailure { handleError(it) }
	}

	fun clickNewChatNotice() {
		startEvent(ChannelEvent.ScrollNewChannelItem)
	}

	fun clickMenuBtn() {
		startEvent(ChannelEvent.OpenOrCloseDrawer)
	}

	private fun startEvent(event: ChannelEvent) = viewModelScope.launch {
		eventFlow.emit(event)
	}

	//TODO : 예외처리 분기 추가해야함 (대부분이 현재 세션 연결 취소 후 다시 재연결 해야 함)
	private suspend fun handleError(throwable: Throwable) {
		withContext(Dispatchers.Main) {
			when (throwable) {
//            is MissingHeartBeatException -> {}
//            is ConnectionException -> {}
//            is LostReceiptException -> {}
				else -> {
					makeToast(R.string.error_network_error)
				}
			}
		}
	}

	private inline fun updateState(block: ChannelUiState.() -> ChannelUiState) {
		_uiStateFlow.update {
			_uiStateFlow.value.block()
		}
	}
}