package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.UserRepository

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