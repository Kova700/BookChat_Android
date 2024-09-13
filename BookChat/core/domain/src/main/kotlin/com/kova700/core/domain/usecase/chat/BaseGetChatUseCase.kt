package com.kova700.core.domain.usecase.chat

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatType
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository

open class BaseGetChatUseCase(
	private val userRepository: UserRepository,
	private val clientRepository: ClientRepository,
) {
	protected suspend fun Chat.attachUser(): Chat {
		val clientId = clientRepository.getClientProfile().id
		val sender = sender?.id?.let { userRepository.getUser(it) }
		val message = when {
			getChatType(clientId) != ChatType.Notice -> this.message
			message.contains("#").not() -> this.message
			else -> {
				val noticeTextList = message.split("#")
				val targetUserId = noticeTextList[1].toLong()
				userRepository.getUser(targetUserId).nickname + noticeTextList.lastOrNull()
			}
		}
		return copy(
			sender = sender,
			message = message
		)
	}
}