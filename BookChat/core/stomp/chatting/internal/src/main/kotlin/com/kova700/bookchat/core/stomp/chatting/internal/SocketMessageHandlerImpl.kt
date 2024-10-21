package com.kova700.bookchat.core.stomp.chatting.internal

import android.util.Log
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.bookchat.core.stomp.chatting.external.SocketMessageHandler
import com.kova700.bookchat.core.stomp.chatting.external.model.CommonMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.NotificationMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.NotificationMessageType
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketMessage
import com.kova700.bookchat.core.stomp.chatting.internal.mapper.toChat
import com.kova700.bookchat.util.Constants.TAG
import javax.inject.Inject

class SocketMessageHandlerImpl @Inject constructor(
	private val chatRepository: ChatRepository,
	private val channelRepository: ChannelRepository,
	private val clientRepository: ClientRepository,
	private val userRepository: UserRepository,
) : SocketMessageHandler {
	override suspend fun handleSocketMessage(
		socketMessage: SocketMessage,
		channelId: Long,
	) {
		when (socketMessage) {
			is CommonMessage -> handleCommonMessage(
				socketMessage = socketMessage,
				channelId = channelId
			)

			is NotificationMessage -> handleNoticeMessage(
				socketMessage = socketMessage,
				channelId = channelId
			)
		}
	}

	private suspend fun handleCommonMessage(
		socketMessage: CommonMessage,
		channelId: Long,
	) {
		val clientId = clientRepository.getClientProfile().id
		val receiptId = socketMessage.receiptId

		val chat = socketMessage.toChat(
			channelId = channelId,
			sender = userRepository.getUser(socketMessage.senderId)
		)

		if (socketMessage.senderId == clientId) {
			updateWaitingChat(chat, receiptId)
			updateChannelLastChat(chat)
			return
		}

		insertNewChat(chat)
		updateChannelLastChat(chat)
	}

	private suspend fun handleNoticeMessage(
		socketMessage: NotificationMessage,
		channelId: Long,
	) {
		val chat = socketMessage.toChat(
			channelId = channelId,
		)

		when (socketMessage.notificationMessageType) {
			NotificationMessageType.NOTICE_ENTER -> {
				channelRepository.enterChannelMember(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return
				)
			}

			NotificationMessageType.NOTICE_EXIT -> {
				channelRepository.leaveChannelMember(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return
				)
			}

			NotificationMessageType.NOTICE_HOST_EXIT ->
				channelRepository.leaveChannelHost(channelId)

			NotificationMessageType.NOTICE_KICK -> {
				val clientId = clientRepository.getClientProfile().id
				val isClientBanned = socketMessage.targetUserId == clientId
				when {
					isClientBanned -> channelRepository.banChannelClient(channelId)
					else -> channelRepository.leaveChannelMember(
						channelId = channelId,
						targetUserId = socketMessage.targetUserId ?: return
					)
				}
			}

			NotificationMessageType.NOTICE_HOST_DELEGATE -> {
				channelRepository.updateChannelHost(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return,
				)
			}

			NotificationMessageType.NOTICE_SUB_HOST_DELEGATE -> {
				channelRepository.updateChannelMemberAuthority(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return,
					channelMemberAuthority = ChannelMemberAuthority.SUB_HOST,
				)
			}

			NotificationMessageType.NOTICE_SUB_HOST_DISMISS ->
				channelRepository.updateChannelMemberAuthority(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return,
					channelMemberAuthority = ChannelMemberAuthority.GUEST,
				)

		}
		insertNewChat(chat)
		updateChannelLastChat(chat)
	}

	private suspend fun insertNewChat(chat: Chat) {
		chatRepository.insertChat(chat)
	}

	private suspend fun updateWaitingChat(chat: Chat, receiptId: Long) {
		Log.d(TAG, "SocketMessageHandlerImpl: updateWaitingChat() - receiptId :$receiptId")
		chatRepository.updateWaitingChat(
			newChat = chat,
			receiptId = receiptId
		)
	}

	private suspend fun updateChannelLastChat(chat: Chat) {
		channelRepository.updateChannelLastChatIfValid(
			channelId = chat.channelId,
			chatId = chat.chatId
		)
	}

}