package com.example.bookchat.data.paging.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChatRepository

@OptIn(ExperimentalPagingApi::class)
class ChatRemoteMediator(
	private val chatRoomId: Long,
	private val chatRepository: ChatRepository,
) : RemoteMediator<Int, Chat>() {

	private var isLast = false
	private var isFirst = true

	override suspend fun load(
		loadType: LoadType,
		state: PagingState<Int, Chat>
	): MediatorResult {

		val loadKey = when (loadType) {
			LoadType.REFRESH -> null
			LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
			LoadType.APPEND -> {
				val lastItem = state.lastItemOrNull()
					?: return MediatorResult.Success(endOfPaginationReached = isLast)
				lastItem.chatId
			}
		}

		return try {
			val response = chatRepository.getChat(
				roomId = chatRoomId,
				size = getLoadSize(),
				isFirst = isFirst,
				postCursorId = loadKey
			)
			isLast = response.cursorMeta.last
			isFirst = false

			MediatorResult.Success(endOfPaginationReached = isLast)
		} catch (e: Exception) {
			MediatorResult.Error(e)
		}
	}

	private fun getLoadSize(): Int = if (isFirst) 3 * CHAT_LOAD_SIZE else CHAT_LOAD_SIZE

	override suspend fun initialize(): InitializeAction {
		return super.initialize()
	}

	companion object {
		private const val CHAT_LOAD_SIZE = 25
	}
}

