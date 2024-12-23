package com.kova700.bookchat.core.stomp.stomp

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object StompClientModule {

	@Provides
	@Singleton
	fun provideWebSocketClient(
		okHttpClient: OkHttpClient,
	): WebSocketClient {
		/** withAutoReconnect : 소켓 재연결만 반영되고 재구독 기능은 아직 개발되지 않음으로 사용 X */
		return OkHttpWebSocketClient(okHttpClient)
	}

	@Provides
	@Singleton
	fun provideStompClient(
		webSocketClient: WebSocketClient,
	): StompClient {
		/** 백엔드측의 send Frame에 대한 receipt 헤더가 사라지는 오류가 있어서 autoReceipt 사용 X */
		return StompClient(
			webSocketClient = webSocketClient,
			configure = {
//				autoReceipt = true
				receiptTimeout = 10.seconds
				heartBeat = HeartBeat(
					minSendPeriod = 10.seconds,
					expectedPeriod = 10.seconds
				)
				defaultSessionCoroutineContext = Dispatchers.IO
//				instrumentation = debugInstrumentation
			})
	}

//	private val debugInstrumentation = object : KrossbowInstrumentation {
//		val TAG = "Stomp"
//		override suspend fun onWebSocketFrameReceived(frame: WebSocketFrame) {
//			super.onWebSocketFrameReceived(frame)
//		}
//
//		override suspend fun onWebSocketClosed(cause: Throwable?) {
//			super.onWebSocketClosed(cause)
//		}
//
//		override suspend fun onWebSocketClientError(exception: Throwable) {
//			super.onWebSocketClientError(exception)
//		}
//
//		override suspend fun onStompFrameSent(frame: StompFrame) {
//			super.onStompFrameSent(frame)
//		}
//
//		override suspend fun onFrameDecoded(
//			originalFrame: WebSocketFrame,
//			decodedFrame: StompFrame,
//		) {
//			super.onFrameDecoded(originalFrame, decodedFrame)
//		}
//	}
}