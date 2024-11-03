package com.kova700.bookchat.core.notification.chat.internal

import android.app.ActivityManager
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
import com.kova700.bookchat.feature.channel.chatting.ChannelActivity
import com.kova700.bookchat.feature.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.kova700.bookchat.util.channel.getBitmap
import com.kova700.bookchat.util.date.toDate
import com.kova700.bookchat.util.user.getBitmap
import com.kova700.core.data.appsetting.external.repository.AppSettingRepository
import com.kova700.core.data.notificationinfo.external.repository.ChattingNotificationInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class ChatNotificationHandlerImpl @Inject constructor(
	@ApplicationContext private val appContext: Context,
	private val iconBuilder: IconBuilder,
	private val chattingNotificationInfoRepository: ChattingNotificationInfoRepository,
	private val appSettingRepository: AppSettingRepository
) : ChatNotificationHandler {

	private val notificationManager =
		(appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
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
		return appSettingRepository.isPushNotificationEnabled()
						&& channel.isNotificationOn
						&& chat.isNewerNotification(notificationId)
						&& isChannelWatching(channel.roomId).not()
	}

	private suspend fun Chat.isNewerNotification(notificationId: Int): Boolean {
		val previousTimeStamp =
			chattingNotificationInfoRepository.getNotificationLastTimestamp(notificationId)
		return (previousTimeStamp == null || previousTimeStamp < timestamp)
	}

	private fun isChannelWatching(channelId: Long): Boolean {
		val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
		val isChannelActivityRunning = activityManager.appTasks
			.any { task -> task.taskInfo.topActivity?.className == ChannelActivity::class.java.name }
		val watchingChannelId = ChannelActivity.watchingChannelId ?: return false
		return isChannelActivityRunning
						&& watchingChannelId == channelId
	}

	private suspend fun getChatNotification(
		chat: Chat,
		sender: User,
		channel: Channel,
		pendingIntent: PendingIntent,
	): Notification {
		val messagingStyle =
			createMessagingStyle(sender, channel).addMessage(chat.toMessagingStyleMessage())

		return NotificationCompat.Builder(appContext, CHATTING_NOTIFICATION_CHANNEL_ID)
			.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
			.setSmallIcon(R.drawable.ic_notification)
			.setColor(ContextCompat.getColor(appContext, R.color.notification_background_orange))
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
		return NotificationCompat.Builder(appContext, CHATTING_NOTIFICATION_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notification)
			.setColor(ContextCompat.getColor(appContext, R.color.notification_background_orange))
			.setAutoCancel(true)
			.setGroup(CHATTING_NOTIFICATION_GROUP_KEY)
			.setGroupSummary(true)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setSubText(appContext.getString(R.string.new_message))
			.build()
	}

	private fun getPendingIntent(channel: Channel): PendingIntent {
		val channelIntent = getChannelActivityIntent(channel)
			.apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
		return PendingIntent.getActivity(
			appContext,
			getNotificationId(channel),
			channelIntent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
		)
	}

	private fun getChannelActivityIntent(channel: Channel): Intent {
		return Intent(appContext, ChannelActivity::class.java).apply {
			putExtra(EXTRA_CHANNEL_ID, channel.roomId)
		}
	}

	private suspend fun createDynamicShortcut(channel: Channel) {
		val shortcutId = getNotificationId(channel).toString()
		val intent = getChannelActivityIntent(channel).setAction(Intent.ACTION_CREATE_SHORTCUT)
		val icon = iconBuilder.buildIcon(
			imageUrl = channel.roomImageUri,
			defaultImage = channel.defaultRoomImageType.getBitmap(appContext)
		)
		val shortcutBuilder = ShortcutInfoCompat.Builder(appContext, shortcutId)
			.setLongLived(true)
			.setIntent(intent)
			.setShortLabel(channel.roomName)
			.setLongLabel(channel.roomName)
			.setIcon(icon)
			.build()
		ShortcutManagerCompat.pushDynamicShortcut(appContext, shortcutBuilder)
	}

	private suspend fun checkAndRemoveNotificationGroup() {
		val chatNotificationCount = notificationManager.activeNotifications
			.count { it.notification.group == CHATTING_NOTIFICATION_GROUP_KEY }
		if (chatNotificationCount > 1) return

		dismissNotification(CHATTING_NOTIFICATION_GROUP_ID)
	}

	private fun checkAndRemoveShortcut(channel: Channel) {
		val shortcutId = getNotificationId(channel).toString()
		ShortcutManagerCompat.getDynamicShortcuts(appContext)
			.find { it.id == shortcutId }
			?.let { ShortcutManagerCompat.removeDynamicShortcuts(appContext, listOf(shortcutId)) }
	}

	private fun clearShortcut() {
		ShortcutManagerCompat.removeAllDynamicShortcuts(appContext)
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
			defaultImage = defaultProfileImageType.getBitmap(appContext)
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