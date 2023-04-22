package com.example.bookchat.api

import android.util.Log
import com.example.bookchat.utils.Constants
import com.example.bookchat.utils.DataStoreManager
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.instrumentation.KrossbowInstrumentation
import org.hildan.krossbow.websocket.WebSocketFrame
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import kotlin.time.Duration.Companion.seconds

object StompBuilder {
    fun getStompClient() = StompClient(
            webSocketClient = OkHttpWebSocketClient(getOkhttpClient()),
            configure = {
                heartBeat = HeartBeat(
                    minSendPeriod = 15.seconds,
                    expectedPeriod = 15.seconds
                )
//                instrumentation = debugInstrumentation
            }
        )
    }

    val debugInstrumentation = object : KrossbowInstrumentation {
        override suspend fun onWebSocketFrameReceived(frame: WebSocketFrame) {
            super.onWebSocketFrameReceived(frame)
            Log.d(Constants.TAG, "ChatRoomViewModel: onWebSocketFrameReceived() - frame : $frame")
        }

        override suspend fun onWebSocketClosed(cause: Throwable?) {
            super.onWebSocketClosed(cause)
            Log.d(Constants.TAG, "ChatRoomViewModel: onWebSocketClosed() - cause :$cause")
        }

        override suspend fun onWebSocketClientError(exception: Throwable) {
            super.onWebSocketClientError(exception)
            Log.d(Constants.TAG, "ChatRoomViewModel: onWebSocketClientError() - exception $exception")
        }

        override suspend fun onStompFrameSent(frame: StompFrame) {
            super.onStompFrameSent(frame)
            Log.d(Constants.TAG, "ChatRoomViewModel: onStompFrameSent() - frame $frame")
        }

        override suspend fun onFrameDecoded(
            originalFrame: WebSocketFrame,
            decodedFrame: StompFrame
        ) {
            super.onFrameDecoded(originalFrame, decodedFrame)
            Log.d(
                Constants.TAG,
                "ChatRoomViewModel: onFrameDecoded() - originalFrame : $originalFrame, decodedFrame :$decodedFrame"
            )
        }
    }

    private fun getOkhttpClient() =
        OkHttpClient.Builder()
            .addInterceptor(AppInterceptor())
            .build()