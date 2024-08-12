package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.UserRepository
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