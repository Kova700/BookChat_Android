package com.kova700.bookchat.core.notification.chat.internal

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
import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.notification.chat.external.ChatNotificationHandler
import com.kova700.bookchat.core.notification.util.iconbuilder.IconBuilder
import com.kova700.bookchat.core.stomp.chatting.external.StompHandler
import com.kova700.bookchat.feature.main.MainActivity
import com.kova700.bookchat.feature.main.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.kova700.bookchat.util.date.toDate
import com.kova700.bookchat.util.image.channel.getBitmap
import com.kova700.bookchat.util.image.user.getBitmap
import com.kova700.core.data.notificationinfo.external.repository.ChattingNotificationInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class ChatNotificationHandlerImpl @Inject constructor(
	@ApplicationContext private val context: Context,
	private val iconBuilder: IconBuilder,
	private val stompHandler: StompHandler,
	private val chattingNotificationInfoRepository: ChattingNotificationInfoRepository,
) : ChatNotificationHandler {

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
		val sender = chat.sender ?: return

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
			sender = sender,
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
			?.let { ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcutId)) }
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
		get() = (dispatchTime.toDate() ?: Date()).time

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
		chattingNotificationInfoRepository.clear()
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