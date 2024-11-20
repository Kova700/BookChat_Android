package com.kova700.bookchat.core.fcm.chat

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.notification.chat.external.ChatNotificationHandler
import com.kova700.core.domain.usecase.channel.GetClientChannelUseCase
import com.kova700.core.domain.usecase.chat.GetChatUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

//TODO : [Version 2] 구독(온라인) 상태임에도 FCM이 수신되는 현상이 있음 + 본인이 보낸 메세지임에도 FCM이 수신되는 상황
@HiltWorker
class ChatNotificationWorker @AssistedInject constructor(
	@Assisted private val appContext: Context,
	@Assisted private val workerParams: WorkerParameters,
	private val channelRepository: ChannelRepository,
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val chatNotificationHandler: ChatNotificationHandler,
	private val getChatUseCase: GetChatUseCase,
	private val getClientChannelUseCase: GetClientChannelUseCase,
) : CoroutineWorker(appContext, workerParams) {

	override suspend fun doWork(): Result {
		if (bookChatTokenRepository.isBookChatTokenExist().not()) return Result.success()
		val channelId: Long = inputData.getLong(EXTRA_CHANNEL_ID, -1)
		val chatId: Long = inputData.getLong(EXTRA_CHAT_ID, -1)
		val senderId: Long = inputData.getLong(EXTRA_SENDER_ID, -1)

		val client = runCatching { clientRepository.getClientProfile() }
			.getOrNull() ?: return Result.failure()
		if (senderId == client.id) return Result.success()

		val apiResult = runCatching {
			val channel = getClientChannelUseCase(channelId) ?: return Result.failure()
			val chat = getChatUseCase(chatId) ?: return Result.failure()
			channelRepository.updateChannelLastChatIfValid(chat.channelId, chat)
			Pair(channel, chat)
		}.getOrNull() ?: return Result.failure()

		val (channel, chat) = apiResult
		if (chat.sender?.id == client.id) return Result.success()

		chatNotificationHandler.showNotification(
			channel = channel,
			chat = chat
		)

		return Result.success()
	}

	companion object {
		private const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
		private const val EXTRA_CHAT_ID = "EXTRA_CHAT_ID"
		private const val EXTRA_SENDER_ID = "EXTRA_SENDER_ID"

		fun start(
			context: Context,
			channelId: Long,
			chatId: Long,
			senderId: Long
		) {
			val loadChatDataWork = OneTimeWorkRequestBuilder<ChatNotificationWorker>()
				.setInputData(
					workDataOf(
						EXTRA_CHANNEL_ID to channelId,
						EXTRA_CHAT_ID to chatId,
						EXTRA_SENDER_ID to senderId
					)
				).build()

			WorkManager
				.getInstance(context)
				.enqueueUniqueWork(
					"$channelId-$chatId",
					ExistingWorkPolicy.APPEND_OR_REPLACE,
					loadChatDataWork
				)
		}

	}

}