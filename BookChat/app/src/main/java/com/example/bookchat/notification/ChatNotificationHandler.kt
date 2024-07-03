package com.example.bookchat.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.example.bookchat.R
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class ChatNotificationHandler @Inject constructor(
	@ApplicationContext private val context: Context,
	private val userIconBuilder: UserIconBuilder,
) : NotificationHandler {

	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	//TODO : 유저정보와 함께 띄우기
	//TODO : 카톡같은 Notification 모양 참고해서 구현
	//TODO : 이미 띄워져 있는 노티가 있을때, 띄워져있던 채팅의 ID가 지금 띄우려는 채팅
	//       ID보다 크다면 노티를 띄우지 않는다.
	//TODO : ChatActivity가 현재 띄워져있다면 노티를 띄우지 않는다.
	//TODO : 노티 띄우기전에 권한 체크

	override suspend fun showNotification(channel: Channel, chat: Chat) {
		chat.sender ?: return
		if (channel.notificationFlag.not()) return

		Log.d(TAG, "ChatNotificationHandler: showNotification() - called")
		val notificationId = channel.roomId.hashCode()

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationChannel = NotificationChannel(
				CHATTING_NOTIFICATION_CHANNEL_ID,
				CHATTING_NOTIFICATION_CHANNEL_NAME,
				NotificationManager.IMPORTANCE_HIGH
			)
			notificationManager.createNotificationChannel(notificationChannel)
		}

		val notification = getChatNotification(
			notificationId = notificationId,
			chat = chat,
			sender = chat.sender,
			channel = channel,
			pendingIntent = getPendingIntent(
				notificationId = notificationId,
				channelId = channel.roomId,
			),
		)

		notificationManager.notify(notificationId, notification)
		Log.d(TAG, "ChatNotificationHandler: showNotification() - finish")
	}

	private suspend fun getChatNotification(
		notificationId: Int,
		chat: Chat,
		sender: User,
		channel: Channel,
		pendingIntent: PendingIntent,
	): Notification {
		val messagingStyle =
			(restoreMessagingStyle(notificationId) ?: createMessagingStyle(sender, channel))
				.addMessage(chat.toMessagingStyleMessage())
		chat.chatId.toString()
//		return NotificationCompat.Builder(context, chat.chatId.toString())
		return NotificationCompat.Builder(context, CHATTING_NOTIFICATION_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_bookchat_app_icon)
//			.setColor(ContextCompat.getColor(context, R.color.transparent))
//			.setColor(Color.GREEN)
//			.setColorized(true)
			.setStyle(messagingStyle)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true) // 클릭 시 알림이 삭제되도록 설정

//			.setOnlyAlertOnce(true)
//			.setGroupSummary(true).apply {
//				priority = NotificationCompat.PRIORITY_HIGH //다시 좀 찾아보기
//			}
//
//			.setStyle(
//				NotificationCompat.BigTextStyle().bigText(chat.message)
//			).apply {
//				priority = NotificationCompat.PRIORITY_HIGH // 다시 좀 찾아보기
//			}

			.build()

	}

	private fun getPendingIntent(
		notificationId: Int,
		channelId: Long,
	): PendingIntent {
		val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
			flags =
				Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //TODO : 굳이 필요한지 다시 확인
			putExtra(MainActivity.EXTRA_NEED_SHOW_CHANNEL_LIST, true)
		}
		val channelActivityIntent = Intent(context, ChannelActivity::class.java).apply {
			putExtra(EXTRA_CHANNEL_ID, channelId)
		}

		val stackBuilder = TaskStackBuilder.create(context).apply {
			addNextIntent(mainActivityIntent)
			addNextIntent(channelActivityIntent)
		}

		return stackBuilder.getPendingIntent(
			notificationId,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	private fun restoreMessagingStyle(notificationId: Int): NotificationCompat.MessagingStyle? =
		notificationManager.activeNotifications
			.firstOrNull { it.id == notificationId }
			?.notification
			?.let(NotificationCompat.MessagingStyle::extractMessagingStyleFromNotification)

	private suspend fun createMessagingStyle(
		sender: User,
		channel: Channel,
	): NotificationCompat.MessagingStyle {
		return NotificationCompat.MessagingStyle(sender.toPerson())
			.setConversationTitle(channel.roomName)
			.setGroupConversation(channel.roomName.isNotBlank())
	}

	private suspend fun Chat.toMessagingStyleMessage(): NotificationCompat.MessagingStyle.Message =
		NotificationCompat.MessagingStyle.Message(message, timestamp, sender?.toPerson())

	private val Chat.timestamp: Long
		get() = (DateManager.stringToDate(dispatchTime) ?: Date()).time

	private suspend fun User.toPerson(): Person =
		Person.Builder()
			.setKey(id.toString())
			.setName(nickname)
			.setIcon(userIconBuilder.buildIcon(this))
			.build()

	override fun dismissChannelNotifications(channelId: Long) {}

	override fun dismissAllNotifications() {}

	companion object {
		private const val CHATTING_NOTIFICATION_CHANNEL_NAME = "BookChatChattingNotificationChannel"
		private const val CHATTING_NOTIFICATION_CHANNEL_ID = "BookChatChattingNotificationChannelId"
	}

}

//서버에서 전송시에 Priorty High로 보내는지 확인
//서버 메세지 time_to_live 시간 4주(기본값) 확인

//TODO : 참고 사항
// FCM 메세지를 알림타입으로 보내면 앱이 forground일때만 Service로 메세지가 전달된다.
// (알림을 그냥 띄워버리니까 그런거 같은 느낌) (서버에서 어떻게 보내는지 확인해볼 것)
// (+ Firebase 콘솔에서 보내는 메세지는 항상 알림 메세지라 Service가 작동하지 않는다.)
// ("Notification messages can contain an optional data payload",
// this will only get submitted to your service,
// if your application is in foreground!)