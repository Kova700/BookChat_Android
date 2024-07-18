package com.example.bookchat.fcm

import android.util.Log
import com.example.bookchat.data.network.model.response.FcmMessage
import com.example.bookchat.domain.model.PushType
import com.example.bookchat.fcm.forcedlogout.ForcedLogoutManagerImpl
import com.example.bookchat.fcm.forcedlogout.ForcedLogoutWorker
import com.example.bookchat.fcm.renewfcmtoken.RenewFcmTokenWorker
import com.example.bookchat.notification.ChatNotificationWorker
import com.example.bookchat.utils.Constants.TAG
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

	@Inject
	lateinit var gson: Gson

	@Inject
	lateinit var logoutPushMessageManager: ForcedLogoutManagerImpl

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		Log.d(TAG, "FCMService: onNewToken() - token :$token")
		startRenewFcmTokenWorker(token)
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		handleMessage(message)
	}

	private fun handleMessage(message: RemoteMessage) {
		val messageBody = message.data["body"]
		val hashMap = gson.fromJson(messageBody, HashMap::class.java)
		when (hashMap["pushType"]) {
			PushType.LOGIN.toString() -> startForcedLogoutWorker()
			PushType.CHAT.toString() ->
				handleChatMessage(gson.fromJson(messageBody, FcmMessage::class.java))
		}
	}

	private fun handleChatMessage(fcmMessage: FcmMessage) {
		startChatNotificationWorker(
			channelId = fcmMessage.body.channelId,
			chatId = fcmMessage.body.chatId
		)
	}

	private fun startChatNotificationWorker(channelId: Long, chatId: Long) {
		ChatNotificationWorker.start(
			context = applicationContext,
			channelId = channelId,
			chatId = chatId
		)
	}

	private fun startRenewFcmTokenWorker(fcmTokenString: String) {
		RenewFcmTokenWorker.start(
			context = applicationContext,
			fcmTokenString = fcmTokenString
		)
	}

	private fun startForcedLogoutWorker() {
		ForcedLogoutWorker.start(applicationContext)
	}

}