package com.kova700.core.domain.usecase.chat

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import javax.inject.Inject

class GetChatUseCase @Inject constructor(
	private val chatRepository: ChatRepository,
	private val userRepository: UserRepository,
	private val clientRepository: ClientRepository,
) : BaseGetChatUseCase(userRepository, clientRepository) {

	suspend operator fun invoke(
		chatId: Long,
	): Chat {
		return chatRepository.getChat(chatId).attachUser()
	}
}