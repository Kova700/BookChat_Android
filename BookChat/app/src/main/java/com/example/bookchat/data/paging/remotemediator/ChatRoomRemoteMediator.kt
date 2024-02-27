package com.example.bookchat.data.paging.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.domain.repository.ChannelRepository

@OptIn(ExperimentalPagingApi::class)
class ChatRoomRemoteMediator(
	private val channelRepository: ChannelRepository
) : RemoteMediator<Int, ChannelEntity>() {

	private var isLast = false
	private var isFirst = true

	private var loadKey: Long? = null

	override suspend fun load(
		loadType: LoadType,
		state: PagingState<Int, ChannelEntity>
	): MediatorResult {

		//TODO : 현재 nextCursorId 채팅방 LastChatID -> 추후 변경 nextCursorId 채팅방 RoomId
		// 수정 대기 중

		loadKey = when (loadType) {
			LoadType.REFRESH -> null
			LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
			LoadType.APPEND -> {
				//TODO : 끝까지 로드하지 않은 상황에서 채팅방 마지막 채팅 업데이트하면
				// 여기서 마지막 페이지 로드 요청 보내는 이슈 있음 위에 API 변경시 같이 해결
//                val lastItem = state.lastItemOrNull()
//                    ?: return MediatorResult.Success(endOfPaginationReached = isLast)
//                lastItem.roomId
				loadKey
			}
		}

		return try {
			val response = channelRepository.getChannels(
				loadSize = getLoadSize(),
//				postCursorId = loadKey
			)

//			loadKey = response.cursorMeta.nextCursorId
//			isLast = response.cursorMeta.last
			isFirst = false

			MediatorResult.Success(endOfPaginationReached = isLast)
		} catch (e: Exception) {
			MediatorResult.Error(e)
		}
	}

	private fun getLoadSize(): Int =
		if (isFirst) 3 * REMOTE_USER_CHAT_ROOM_LOAD_SIZE
		else REMOTE_USER_CHAT_ROOM_LOAD_SIZE

	override suspend fun initialize(): InitializeAction {
		return super.initialize()
	}

	companion object {
		const val REMOTE_USER_CHAT_ROOM_LOAD_SIZE = 7
	}
}