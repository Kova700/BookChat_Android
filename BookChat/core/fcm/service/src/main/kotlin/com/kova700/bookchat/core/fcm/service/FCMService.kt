package com.kova700.bookchat.core.fcm.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kova700.bookchat.core.fcm.chat.ChatNotificationWorker
import com.kova700.bookchat.core.fcm.forced_logout.ForcedLogoutWorker
import com.kova700.bookchat.core.fcm.forced_logout.model.ForcedLogoutReason
import com.kova700.bookchat.core.fcm.renew_fcm_token.RenewFcmTokenWorker
import com.kova700.bookchat.core.fcm.service.model.ChatFcmBody
import com.kova700.bookchat.core.fcm.service.model.FcmMessage
import com.kova700.bookchat.core.fcm.service.model.PushType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

	@Inject
	lateinit var jsonSerializer: Json

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		startRenewFcmTokenWorker(token)
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		handleMessage(message)
	}

	private fun handleMessage(message: RemoteMessage) {
		val messageBody = message.data["body"] ?: return
		val fcmMessage = jsonSerializer.decodeFromString<FcmMessage>(messageBody)
		when (fcmMessage.pushType) {
			PushType.LOGIN -> startForcedLogoutWorker()
			PushType.CHAT -> handleChatMessage(fcmMessage)
		}
	}

	private fun handleChatMessage(fcmMessage: FcmMessage) {
		if (fcmMessage.body !is ChatFcmBody) return
		startChatNotificationWorker(
			channelId = fcmMessage.body.channelId,
			chatId = fcmMessage.body.chatId,
			senderId = fcmMessage.body.senderId
		)
	}

	private fun startChatNotificationWorker(
		channelId: Long,
		chatId: Long,
		senderId: Long
	) {
		ChatNotificationWorker.start(
			context = applicationContext,
			channelId = channelId,
			chatId = chatId,
			senderId = senderId
		)
	}

	private fun startRenewFcmTokenWorker(fcmTokenString: String) {
		RenewFcmTokenWorker.start(
			context = applicationContext,
			fcmTokenString = fcmTokenString
		)
	}

	private fun startForcedLogoutWorker() {
		ForcedLogoutWorker.start(
			context = applicationContext,
			reason = ForcedLogoutReason.CHANGE_DEVICE
		)
	}

}