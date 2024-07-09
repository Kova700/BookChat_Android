package com.example.bookchat.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import com.example.bookchat.R
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.example.bookchat.utils.DateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class ChatNotificationHandler @Inject constructor(
	@ApplicationContext private val context: Context,
	private val iconBuilder: IconBuilder,
) : NotificationHandler {

	private val notificationManager =
		(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
			.apply {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					val notificationChannel = NotificationChannel(
						CHATTING_NOTIFICATION_CHANNEL_ID,
						CHATTING_NOTIFICATION_CHANNEL_NAME,
						NotificationManager.IMPORTANCE_HIGH
					)
					createNotificationChannel(notificationChannel)
				}
			}


	//TODO : 이미 띄워져 있는 노티가 있을때, 띄워져있던 채팅의 ID가 지금 띄우려는 채팅
	//        ID보다 크다면 노티를 띄우지 않는다.
	//TODO : ChannelActivity가 현재 Notification에 해당하는 채널을 띄우고 았다면 노티를 띄우지 않는다.
	//TODO : 노티 띄우기전에 권한 체크
	//TODO : 노티를 눌러서 ChannelActivity로 이동한게 아닌 직접 유저가 눌러서 해당 채팅방에 들어가더라도
	//        해당 채팅방에 해당하는 노티는 제거
	//TODO : 카톡처럼 Doze 상태에도 노티 받을 수 있게 권한 받기

	override suspend fun showNotification(channel: Channel, chat: Chat) {
		chat.sender ?: return
		if (channel.notificationFlag.not()) return

		val notificationId = getNotificationId(channel)
		createDynamicShortcut(channel)

		val notification = getChatNotification(
			chat = chat,
			sender = chat.sender,
			channel = channel,
			pendingIntent = getPendingIntent(channel)
		)
		notificationManager.notify(notificationId, notification)
		notificationManager.notify(0, getGroupNotification())
	}

	//TODO : 우측 화살표 아래 쌓인 노티 메세지 개수만 표시하고 addMessage하지말자
	//TODO : 메세지 최대길이, 줄바꿈, 라인 수 제한(1) 설정
	private suspend fun getChatNotification(
		chat: Chat,
		sender: User,
		channel: Channel,
		pendingIntent: PendingIntent,
	): Notification {
		val messagingStyle =
			createMessagingStyle(sender, channel).addMessage(chat.toMessagingStyleMessage())

		return NotificationCompat.Builder(context, CHATTING_NOTIFICATION_CHANNEL_ID)
			.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
			.setSmallIcon(R.drawable.ic_notification)
			.setColor(ContextCompat.getColor(context, R.color.notification_background_orange))
			.setStyle(messagingStyle)
			.setShortcutId(getNotificationId(channel).toString())
			.setGroup(CHATTING_NOTIFICATION_GROUP_ID)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.build()
	}

	private fun getGroupNotification(): Notification {
		return NotificationCompat.Builder(context, CHATTING_NOTIFICATION_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notification)
			.setColor(ContextCompat.getColor(context, R.color.notification_background_orange))
			.setAutoCancel(true)
			.setGroup(CHATTING_NOTIFICATION_GROUP_ID)
			.setGroupSummary(true)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setSubText(context.getString(R.string.new_message))
			.build()
	}

	private fun getPendingIntent(channel: Channel): PendingIntent {
		return PendingIntent.getActivity(
			context,
			getNotificationId(channel),
			getMainActivityIntent(channel),
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
	}

	private fun getMainActivityIntent(channel: Channel): Intent {
		return Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			putExtra(MainActivity.EXTRA_NEED_SHOW_CHANNEL_LIST, true)
			putExtra(EXTRA_CHANNEL_ID, channel.roomId)
		}
	}

	private suspend fun createDynamicShortcut(channel: Channel) {
		val shortcutId = getNotificationId(channel).toString()
		val intent = getMainActivityIntent(channel).setAction(Intent.ACTION_CREATE_SHORTCUT)
		val shortcutBuilder = ShortcutInfoCompat.Builder(context, shortcutId)
			.setLongLived(true)
			.setIntent(intent)
			.setShortLabel(channel.roomName)
			.setLongLabel(channel.roomName)
			.setIcon(iconBuilder.buildIcon(channel.roomImageUri))
			.build()
		ShortcutManagerCompat.pushDynamicShortcut(context, shortcutBuilder)
	}

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
			.setIcon(iconBuilder.buildIcon(profileImageUrl))
			.build()

	private fun getNotificationId(channel: Channel): Int {
		return channel.roomId.hashCode()
	}

	override fun dismissChannelNotifications(channelId: Long) {}

	override fun dismissAllNotifications() {}

	companion object {
		private const val CHATTING_NOTIFICATION_GROUP_ID = "BookChatChattingNotificationGroupId"
		private const val CHATTING_NOTIFICATION_CHANNEL_NAME = "BookChatChattingNotificationChannel"
		private const val CHATTING_NOTIFICATION_CHANNEL_ID = "BookChatChattingNotificationChannelId"
	}

}