package com.example.bookchat.fcm

import android.util.Log
import com.example.bookchat.data.network.model.response.FcmMessage
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.PushType
import com.example.bookchat.domain.usecase.LogoutUseCase
import com.example.bookchat.notification.ChatNotificationWorker
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

	//TODO : Background 작업 WorkerManager로 위임

	@Inject
	lateinit var gson: Gson

	@Inject
	lateinit var logoutUseCase: LogoutUseCase

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		renewFCMToken(FCMToken(text = token))
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		Log.d(TAG, "FCMService: onMessageReceived() - message.data : ${message.data}")
		handleMessage(message)
	}

	private fun handleMessage(message: RemoteMessage) {
		val messageBody = message.data["body"]
		val hashMap = gson.fromJson(messageBody, LinkedHashMap::class.java)
		when (hashMap["pushType"]) {
			PushType.LOGIN.toString() -> logout()
			PushType.CHAT.toString() ->
				handleChatMessage(gson.fromJson(messageBody, FcmMessage::class.java))
		}
	}

	private fun handleChatMessage(fcmMessage: FcmMessage) {
		startNotificationWorker(
			channelId = fcmMessage.body.channelId,
			chatId = fcmMessage.body.chatId
		)
	}

	private fun startNotificationWorker(channelId: Long, chatId: Long) {
		ChatNotificationWorker.start(
			context = applicationContext,
			channelId = channelId,
			chatId = chatId
		)
	}

	//TODO : WorkerManager로 백엔드 작업 위임 (예외처리까지 같이)
	private fun renewFCMToken(fcmToken: FCMToken) {
		CoroutineScope(Dispatchers.IO).launch {
			fcmTokenRepository.renewFCMToken(fcmToken)
		}
		//1.    로컬 DB에 FCM 토큰 저장
		//2-1.  access 토큰을 가지고 있다면, 저장된 FCM 토큰 서버로 전송
		//2-2.  없다면, 전송 x
		//3.    토큰 만료 혹은 API 실패로 해당 토큰이 서버로 전달 안되었을 경우를 감안해
		//      로그인 API 다음 이어서 매번 서버에 FCM토큰 갱신 API호출해서 서버에 매번 덮어쓰기
		//      혹은 전송 실패시 Flag 기록하고 앱 켤 때마다 확인 후 True라면 API 호출 되게 최적화 가능
	}

	private fun logout() {
		//TODO : WorkerManager로 백엔드 작업
		// TODO : 앱이 켜져 있었다면 로그인 페이지로 이동 혹은 이동할 수 있는 Dialog 노출
		//    켜져 있지 않았다면 그냥 데이터만 제거
		logoutUseCase(needServer = false)
	}

}
