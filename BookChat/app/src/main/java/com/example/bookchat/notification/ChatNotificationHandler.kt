package com.example.bookchat.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
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
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class ChatNotificationHandler @Inject constructor(
	@ApplicationContext private val context: Context,
	private val iconBuilder: IconBuilder,
) : NotificationHandler {

	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	//TODO : 유저정보와 함께 띄우기
	//TODO : 카톡같은 Notification 모양 참고해서 구현
	//TODO : 이미 띄워져 있는 노티가 있을때, 띄워져있던 채팅의 ID가 지금 띄우려는 채팅
	//       ID보다 크다면 노티를 띄우지 않는다.
	//TODO : ChatActivity가 현재 띄워져있다면 노티를 띄우지 않는다.
	//TODO : 노티 띄우기전에 권한 체크
	//TODO : 펼쳐져있을 땐, 채팅방 이미지가 나와야함
	//TODO : 해당 채팅방에 들어가면 해당 채팅방에 해당하는 노티는 제거

	override suspend fun showNotification(channel: Channel, chat: Chat) {
		chat.sender ?: return
		if (channel.notificationFlag.not()) return

		val notificationId = getNotificationId(channel)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationChannel = NotificationChannel(
				CHATTING_NOTIFICATION_CHANNEL_ID,
				CHATTING_NOTIFICATION_CHANNEL_NAME,
				NotificationManager.IMPORTANCE_HIGH
			)
			notificationManager.createNotificationChannel(notificationChannel)
		}

		createDynamicShortcut(channel)

		val notification = getChatNotification(
			notificationId = notificationId,
			chat = chat,
			sender = chat.sender,
			channel = channel,
			pendingIntent = getPendingIntent(channel)
		)
		notificationManager.notify(notificationId, notification)
		notificationManager.notify(0, getGroupNotification())
		Log.d(TAG, "ChatNotificationHandler: showNotification() - finish")
	}

	//TODO : 우측 화살표 아래 쌓인 노티 메세지 개수만 표시하고 addMessage하지말자
	//TODO : 메세지 최대길이, 줄바꿈, 라인 수 제한(1) 설정
	private suspend fun getChatNotification(
		notificationId: Int,
		chat: Chat,
		sender: User,
		channel: Channel,
		pendingIntent: PendingIntent,
	): Notification {
		//TODO : restoreMessagingStyle 잘 작동하는지 확인
		val messagingStyle =
			(restoreMessagingStyle(notificationId) ?: createMessagingStyle(sender, channel))
				.addMessage(chat.toMessagingStyleMessage()) //TODO : 굳이 필요한지 다시 확인
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
			.build()
	}

	//TODO : 누르면 그냥 채팅방 입장 없이 채팅방 목록 Fragment로 이동되게 pendingIntent 추가
	private fun getGroupNotification(): Notification {
		return NotificationCompat.Builder(context, CHATTING_NOTIFICATION_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notification)
			.setColor(ContextCompat.getColor(context, R.color.notification_background_orange))
			.setAutoCancel(true)
			.setOnlyAlertOnce(true)
			.setGroup(CHATTING_NOTIFICATION_GROUP_ID)
			.setGroupSummary(true).apply {
				priority = NotificationCompat.PRIORITY_HIGH //다시 좀 찾아보기
			}
			.setSubText(context.getString(R.string.new_message))
			.build()
	}

	private fun getPendingIntent(
		channel: Channel,
	): PendingIntent {
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
			//TODO : flags 굳이 필요한지 다시 확인
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

	private fun restoreMessagingStyle(notificationId: Int): NotificationCompat.MessagingStyle? =
		notificationManager.activeNotifications
			.firstOrNull { it.id == notificationId }
			?.notification
			?.let(NotificationCompat.MessagingStyle::extractMessagingStyleFromNotification)

	//여기 notification Id를 지정하는 코드가 없는데 restoreMessagingStyle에서 어떻게 아이디를 통해서
	//가져올 수가 있는 거지
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

	private suspend fun Channel.toPerson(): Person =
		Person.Builder()
			.setKey(roomId.toString())
			.setName(roomName)
			.setIcon(iconBuilder.buildIcon(roomImageUri))
			.build()

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

//서버에서 전송시에 Priorty High로 보내는지 확인
//서버 메세지 time_to_live 시간 4주(기본값) 확인

//TODO : 참고 사항
// FCM 메세지를 알림타입으로 보내면 앱이 forground일때만 Service로 메세지가 전달된다.
// (알림을 그냥 띄워버리니까 그런거 같은 느낌) (서버에서 어떻게 보내는지 확인해볼 것)
// (+ Firebase 콘솔에서 보내는 메세지는 항상 알림 메세지라 Service가 작동하지 않는다.)
// ("Notification messages can contain an optional data payload",
// this will only get submitted to your service,
// if your application is in foreground!)