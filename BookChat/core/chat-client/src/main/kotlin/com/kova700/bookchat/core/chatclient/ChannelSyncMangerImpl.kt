package com.kova700.bookchat.core.chatclient

import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.core.domain.usecase.channel.GetClientChannelInfoUseCase
import com.kova700.core.domain.usecase.chat.SyncChannelChatsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : [Version 2] EventHistory 받는 로직으로 동기화 방식 수정
//TODO : [Version 2] getChannelInfo를 소켓 재연결시마다 모든 채팅방에 호출하기 부담스러움으로
//        Version 1에서는 현재 Watching 중인 단일 채팅방에 한해서만 사용되게 사용할 것
//        EventHistory 받는 로직이 완성되면 getChannelInfo를 이용한 동기화 로직은 삭제 예정
class ChannelSyncMangerImpl @Inject constructor(
	private val syncChannelChatsUseCase: SyncChannelChatsUseCase,
	private val getClientChannelInfoUseCase: GetClientChannelInfoUseCase,
	private val networkManager: NetworkManager,
) : ChannelSyncManger {
	private val channelSyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	override fun sync(channelIds: Collection<Long>) {
		channelIds.forEach { channelId ->
			syncChats(channelId)
			getChannelInfo(channelId)
		}
	}

	/** 소켓이 끊긴 사이에 발생한 서버와 클라이언트간의 채팅 불일치 동기화 */
	private fun syncChats(channelId: Long) = channelSyncScope.launch {
		if (networkManager.isNetworkAvailable().not()) return@launch
		syncChannelChatsUseCase(channelId)
	}

	/** 소켓이 끊긴 사이에 발생한 서버와 클라이언트 간의 데이터 불일치를 메우기 위해서
	 * 리커넥션 시마다 호출 (이벤트 History받는 로직 구현 이전까지 임시로 사용)*/
	private fun getChannelInfo(channelId: Long) = channelSyncScope.launch {
		if (networkManager.isNetworkAvailable().not()) return@launch
		getClientChannelInfoUseCase.invoke(channelId)
	}

}