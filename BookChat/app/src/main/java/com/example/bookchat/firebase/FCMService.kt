package com.example.bookchat.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.FCMPushMessage
import com.example.bookchat.data.repository.ChattingRepositoryFacade
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.PushType
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.Constants.TAG
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
	@Inject
	lateinit var gson: Gson

	@Inject
	lateinit var clientRepository: ClientRepository

	@Inject
	lateinit var chattingRepositoryFacade: ChattingRepositoryFacade

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		renewFCMToken(FCMToken(text = token))
		//1.    로컬 DB에 FCM 토큰 저장
		//2-1.  access 토큰을 가지고 있다면, 저장된 FCM 토큰 서버로 전송
		//2-2.  없다면, 전송 x
		//3.    토큰 만료 혹은 API 실패로 해당 토큰이 서버로 전달 안되었을 경우를 감안해
		//      로그인 API 다음 이어서 매번 서버에 FCM토큰 갱신 API호출해서 서버에 매번 덮어쓰기
		//      혹은 전송 실패시 Flag 기록하고 앱 켤 때마다 확인 후 True라면 API 호출 되게 최적화 가능
	}

	//TODO : WorkerManager로 백엔드 작업 위임 (예외처리까지 같이)
	private fun renewFCMToken(fcmToken: FCMToken) {
		CoroutineScope(Dispatchers.IO).launch {
			clientRepository.renewFCMToken(fcmToken)
		}
	}

	//TODO : 내가 보낸 메세지도 FCM 오는지 체크하고 말해주기
	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		Log.d(TAG, "FCMService: onMessageReceived() - message.data : ${message.data}")
		//FCMService: onMessageReceived() - message.data : {body={"pushType":"CHAT","body":1485}, title=Book Chat}
		handleMessage(message)
	}

	private fun handleMessage(message: RemoteMessage) {
		val hashMap = gson.fromJson(message.data["body"], LinkedHashMap::class.java)
		when (hashMap["pushType"]) {
			PushType.LOGIN.toString() -> handleLogoutMassage()
			PushType.CHAT.toString() ->
				handleChatMessage(gson.fromJson(message.data["body"], FCMPushMessage::class.java))
		}
	}

	// TODO : 로그인 페이지로 이동 혹은 이동할 수 있는 Dialog 노출
	private fun handleLogoutMassage() {
		//clientRepository.signOut()
	}

	//TODO : WorkerManager로 백엔드 작업 위임 (예외처리까지 같이)
	private fun handleChatMessage(fcmPushMessage: FCMPushMessage) {
		CoroutineScope(Dispatchers.IO).launch {
			val chat = chattingRepositoryFacade.getChatForFCM(fcmPushMessage.chatId)

			if (chattingRepositoryFacade.isAlreadyEntered(chat.chatRoomId).not()) {
				chattingRepositoryFacade.getChannelForFCM(chat)
			}
			sendNotification(chat)
		}
	}

	//TODO : 유저정보와 함께 띄우기
	private fun sendNotification(chat: Chat) {
		val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val importance = NotificationManager.IMPORTANCE_HIGH
			val notificationChannel = NotificationChannel(channelId, channelName, importance)
			notificationChannel.description = channelDescription

			// 채널에 대한 각종 설정(불빛, 진동 등) (추후 다시 세팅)
			notificationChannel.enableLights(true)
			notificationChannel.lightColor = Color.RED
			notificationChannel.enableVibration(true)
			notificationChannel.vibrationPattern = longArrayOf(100L, 200L, 300L)
			notificationManager.createNotificationChannel(notificationChannel)
		}

		notificationManager.notify(chat.chatRoomId.toInt(), getChatNotification(chat))
		notificationManager.notify(0, getGroupNotification())
	}

	private fun getChatNotification(chat: Chat): Notification {
		return NotificationCompat.Builder(this, channelId)
			.setSmallIcon(R.mipmap.ic_bookchat_app_icon) //TODO : 유저 이미지로 수정
			.setAutoCancel(true) // 클릭 시 알림이 삭제되도록 설정
			.setGroup(KEY_BOOK_CHAT_GROUP)
			.setWhen(System.currentTimeMillis()) //TODO : fcmBody.dispatchTime으로 수정
			.setContentTitle(chat.chatRoomId.toString()) //TODO : DB에서 채팅방명 가져오게 수정
			.setContentText(chat.message)
			// 알림과 동시에 진동 설정(권한 필요)
			.setDefaults(Notification.DEFAULT_VIBRATE)
			.setStyle(
				NotificationCompat.BigTextStyle().bigText(chat.message)
			).apply {
				priority = NotificationCompat.PRIORITY_HIGH //다시 좀 찾아보기
			}.build()
	}

	private fun getGroupNotification(): Notification {
		return NotificationCompat.Builder(this, channelId)
			.setSmallIcon(R.mipmap.ic_bookchat_app_icon)
			.setAutoCancel(true)
			.setOnlyAlertOnce(true)
			.setGroup(KEY_BOOK_CHAT_GROUP)
			.setGroupSummary(true).apply {
				priority = NotificationCompat.PRIORITY_HIGH //다시 좀 찾아보기
			}
			.build()
	}

	companion object {
		private const val KEY_BOOK_CHAT_GROUP = "KEY_BOOK_CHAT_GROUP"
		private const val channelId = "channelId"
		private const val channelName = "BookChatNotificationChannel"
		private const val channelDescription = "북챗 푸시 알림 채널"
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