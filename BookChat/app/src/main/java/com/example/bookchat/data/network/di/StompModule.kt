package com.example.bookchat.data.network.di

import android.util.Log
import com.example.bookchat.utils.Constants.TAG
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.instrumentation.KrossbowInstrumentation
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.WebSocketFrame
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.hildan.krossbow.websocket.reconnection.withAutoReconnect
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
		return OkHttpWebSocketClient(okHttpClient)
			.withAutoReconnect()
	}

	@Provides
	@Singleton
	fun provideStompClient(
		webSocketClient: WebSocketClient,
	): StompClient {
		return StompClient(
			webSocketClient = webSocketClient,
			configure = {
				autoReceipt = true
				receiptTimeout = 10.seconds
				heartBeat = HeartBeat(
					minSendPeriod = 15.seconds,
					expectedPeriod = 15.seconds
				)
				instrumentation = debugInstrumentation
			})
	}
	//TODO :직렬화 로직 추가 가능한지 확인

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
			decodedFrame: StompFrame
		) {
			super.onFrameDecoded(originalFrame, decodedFrame)
			Log.d(TAG, "StompModule: onFrameDecoded() - originalFrame : $originalFrame")
			Log.d(TAG, "StompModule: onFrameDecoded() - decodedFrame : $decodedFrame")
		}
	}
}