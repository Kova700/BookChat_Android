package com.example.bookchat.data.network.di

import android.util.Log
import com.example.bookchat.utils.Constants.TAG
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.instrumentation.KrossbowInstrumentation
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.WebSocketFrame
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object StompModule {

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

	private val debugInstrumentation = object : KrossbowInstrumentation {
		override suspend fun onWebSocketFrameReceived(frame: WebSocketFrame) {
			super.onWebSocketFrameReceived(frame)
			Log.d(TAG, "StompModule: onWebSocketFrameReceived() - frame : $frame")
		}

		override suspend fun onWebSocketClosed(cause: Throwable?) {
			super.onWebSocketClosed(cause)
			Log.d(TAG, "StompModule: onWebSocketClosed() - cause : $cause")
		}

		override suspend fun onWebSocketClientError(exception: Throwable) {
			super.onWebSocketClientError(exception)
			Log.d(TAG, "StompModule: onWebSocketClientError() - exception : $exception")
		}

		override suspend fun onStompFrameSent(frame: StompFrame) {
			super.onStompFrameSent(frame)
			Log.d(TAG, "StompModule: onStompFrameSent() - frame : $frame")
		}

		override suspend fun onFrameDecoded(
			originalFrame: WebSocketFrame,
			decodedFrame: StompFrame,
		) {
			super.onFrameDecoded(originalFrame, decodedFrame)
			Log.d(TAG, "StompModule: onFrameDecoded() - originalFrame : $originalFrame")
			Log.d(TAG, "StompModule: onFrameDecoded() - decodedFrame : $decodedFrame")
		}
	}
}