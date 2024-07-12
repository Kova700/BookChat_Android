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
import com.example.bookchat.domain.repository.ChattingNotificationInfoRepository
import com.example.bookchat.domain.repository.StompHandler
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.example.bookchat.ui.mapper.getBitmap
import com.example.bookchat.utils.DateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class ChatNotificationHandler @Inject constructor(
	@ApplicationContext private val context: Context,
	private val iconBuilder: IconBuilder,
	private val stompHandler: StompHandler,
	private val chattingNotificationInfoRepository: ChattingNotificationInfoRepository,
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

	override suspend fun showNotification(channel: Channel, chat: Chat) {
		if (chat.sender == null) return

		val notificationId = getNotificationId(channel)
		if (shouldShowNotification(
				channel = channel,
				chat = chat,
				notificationId = notificationId
			).not()
		) return

		createDynamicShortcut(channel)

		val notification = getChatNotification(
			chat = chat,
			sender = chat.sender,
			channel = channel,
			pendingIntent = getPendingIntent(channel)
		)
		chattingNotificationInfoRepository.updateShownNotificationInfo(
			notificationId = notificationId,
			lastTimestamp = chat.timestamp
		)
		notificationManager.notify(notificationId, notification)
		notificationManager.notify(CHATTING_NOTIFICATION_GROUP_ID, getGroupNotification())
	}

	private suspend fun shouldShowNotification(
		channel: Channel,
		chat: Chat,
		notificationId: Int,
	): Boolean {
		val previousTimeStamp =
			chattingNotificationInfoRepository.getNotificationLastTimestamp(notificationId)
		return (previousTimeStamp == null || previousTimeStamp < chat.timestamp)
						&& channel.notificationFlag
						&& stompHandler.isSocketConnected(channel.roomId).not()
	}

	private suspend fun getChatNotification(
		chat: Chat,
		sender: User,
		channel: Channel,
		pendingIntent: PendingIntent,
	): Notification {
		val messagingStyle =
			createMessagingStyle(sender, channel).addMessage(chat.toMessagingStyleMessage())

		messagingStyle.messages.firstOrNull()?.timestamp

		return NotificationCompat.Builder(context, CHATTING_NOTIFICATION_CHANNEL_ID)
			.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
			.setSmallIcon(R.drawable.ic_notification)
			.setColor(ContextCompat.getColor(context, R.color.notification_background_orange))
			.setStyle(messagingStyle)
			.setShortcutId(getNotificationId(channel).toString())
			.setGroup(CHATTING_NOTIFICATION_GROUP_KEY)
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
			.setGroup(CHATTING_NOTIFICATION_GROUP_KEY)
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
			putExtra(EXTRA_CHANNEL_ID, channel.roomId)
		}
	}

	//TODO : channel profileImageUrl 없으면 default Image로 icon 만들게 수정 (테스트 필요)
	private suspend fun createDynamicShortcut(channel: Channel) {
		val shortcutId = getNotificationId(channel).toString()
		val intent = getMainActivityIntent(channel).setAction(Intent.ACTION_CREATE_SHORTCUT)

		val icon = iconBuilder.buildIcon(
			imageUrl = channel.roomImageUri,
			defaultImage = channel.defaultRoomImageType.getBitmap(context)
		)
		val shortcutBuilder = ShortcutInfoCompat.Builder(context, shortcutId)
			.setLongLived(true)
			.setIntent(intent)
			.setShortLabel(channel.roomName)
			.setLongLabel(channel.roomName)
			.setIcon(icon)
			.build()
		ShortcutManagerCompat.pushDynamicShortcut(context, shortcutBuilder)
	}

	private suspend fun checkAndRemoveNotificationGroup() {
		val chatNotificationCount = notificationManager.activeNotifications
			.count { it.notification.group == CHATTING_NOTIFICATION_GROUP_KEY }
		if (chatNotificationCount > 1) return

		dismissNotification(CHATTING_NOTIFICATION_GROUP_ID)
	}

	private fun checkAndRemoveShortcut(channel: Channel) {
		val shortcutId = getNotificationId(channel).toString()
		ShortcutManagerCompat.getDynamicShortcuts(context)
			.find { it.id == shortcutId }
			?.let {
				ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcutId))
			}
	}

	private fun clearShortcut() {
		ShortcutManagerCompat.removeAllDynamicShortcuts(context)
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

	//TODO : user profileImageUrl 없으면 default Image로 icon 만들게 수정 (테스트 필요)
	private suspend fun User.toPerson(): Person {
		val icon = iconBuilder.buildIcon(
			imageUrl = profileImageUrl,
			defaultImage = defaultProfileImageType.getBitmap(context)
		)
		return Person.Builder()
			.setKey(id.toString())
			.setName(nickname)
			.setIcon(icon)
			.build()
	}

	private fun getNotificationId(channel: Channel): Int {
		return channel.roomId.hashCode()
	}

	override suspend fun dismissChannelNotifications(channel: Channel) {
		dismissNotification(getNotificationId(channel))
		checkAndRemoveShortcut(channel)
		checkAndRemoveNotificationGroup()
	}

	override suspend fun dismissAllNotifications() {
		notificationManager.cancelAll()
		chattingNotificationInfoRepository.clearShownNotificationInfos()
		clearShortcut()
	}

	override suspend fun dismissNotification(notificationId: Int) {
		notificationManager.cancel(notificationId)
		chattingNotificationInfoRepository.removeShownNotificationInfo(notificationId)
	}

	companion object {
		private val CHATTING_NOTIFICATION_GROUP_ID = "BookChatChattingNotificationGroupId".hashCode()
		private const val CHATTING_NOTIFICATION_GROUP_KEY = "BookChatChattingNotificationGroupKey"
		private const val CHATTING_NOTIFICATION_CHANNEL_NAME = "BookChatChattingNotificationChannel"
		private const val CHATTING_NOTIFICATION_CHANNEL_ID = "BookChatChattingNotificationChannelId"
	}

}