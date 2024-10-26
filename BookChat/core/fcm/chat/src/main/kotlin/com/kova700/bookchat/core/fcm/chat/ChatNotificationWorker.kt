package com.kova700.bookchat.core.fcm.chat

import android.content.Context
import android.util.Log
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
		Log.d("ㄺ", "ChatNotificationWorker: doWork() - just called")
		if (bookChatTokenRepository.isBookChatTokenExist().not()) return Result.success()

		val channelId: Long = inputData.getLong(EXTRA_CHANNEL_ID, -1)
		val chatId: Long = inputData.getLong(EXTRA_CHAT_ID, -1)

		//TODO : [FixWaiting] SenderId를 함께 넘겨 받아서 만약 Sender가 클라이언트라면 아래 API호출하지 않게 수정
		val apiResult = runCatching {
			val channel = getClientChannelUseCase(channelId)
			val chat = getChatUseCase(chatId)
			val client = clientRepository.getClientProfile()
			Log.d("ㄺ", "ChatNotificationWorker: doWork() - real Work")
			channelRepository.updateChannelLastChatIfValid(chat.channelId, chat.chatId)
			Triple(channel, chat, client)
		}.getOrNull() ?: return Result.failure()

		val (channel, chat, client) = apiResult
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

		fun start(
			context: Context,
			channelId: Long,
			chatId: Long,
		) {
			val loadChatDataWork = OneTimeWorkRequestBuilder<ChatNotificationWorker>()
				.setInputData(
					workDataOf(
						EXTRA_CHANNEL_ID to channelId,
						EXTRA_CHAT_ID to chatId
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