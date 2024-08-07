package com.example.bookchat.fcm.noticechat

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bookchat.domain.repository.AppSettingRepository
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.notification.chat.ChatNotificationHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ChatNotificationWorker @AssistedInject constructor(
	@Assisted appContext: Context,
	@Assisted workerParams: WorkerParameters,
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository,
	private val appSettingRepository: AppSettingRepository,
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val chatNotificationHandler: ChatNotificationHandler,
) : CoroutineWorker(appContext, workerParams) {

	override suspend fun doWork(): Result {
		if (bookChatTokenRepository.isBookChatTokenExist().not()
			|| appSettingRepository.isPushNotificationEnabled().not()
		) {
			return Result.success()
		}

		val channelId: Long = inputData.getLong(EXTRA_CHANNEL_ID, -1)
		val chatId: Long = inputData.getLong(EXTRA_CHAT_ID, -1)
		//TODO : SenderId를 함께 넘겨 받아서 만약 Sender가 클라이언트라면 아래 API호출하지 않게 수정
		runCatching {
			val channel = channelRepository.getChannel(channelId)
			val chat = chatRepository.getChat(chatId)
			val client = clientRepository.getClientProfile()
			channelRepository.updateChannelLastChatIfValid(chat.chatRoomId, chat.chatId)
			Triple(channel, chat, client)
		}
			.onSuccess {
				val (channel, chat, client) = it
				if (chat.sender?.id == client.id) return@onSuccess

				chatNotificationHandler.showNotification(
					channel = channel,
					chat = chat
				)
				return Result.success()
			}
		return Result.failure()
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