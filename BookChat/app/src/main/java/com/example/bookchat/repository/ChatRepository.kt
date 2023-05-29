package com.example.bookchat.repository

import android.util.Log
import com.example.bookchat.App
import com.example.bookchat.BuildConfig
import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.request.RequestSendChat
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import javax.inject.Inject

class ChatRepository @Inject constructor() {

    // TODO :SEND를 제외한 모든 Frame에 Receipt받게 헤더 수정 필요함

    suspend fun getStompSession(): StompSession {
        Log.d(TAG, "ChatRepository: connectSocket() - called")
        return App.instance.stompClient.connect(
            url = BuildConfig.STOMP_CONNECTION_URL,
            customStompConnectHeaders = getHeader()
        )
    }

    //이렇게 보내면 토큰 자동 갱신은 어케 하누?
    suspend fun subscribeChatTopic(
        stompSession: StompSession,
        roomSid: String
    ): Flow<SocketMessage> {
        Log.d(TAG, "ChatRepository: subscribeChatRoom() - called")
        return stompSession.subscribe(
            StompSubscribeHeaders(
                destination = "$SUB_CHAT_ROOM_DESTINATION$roomSid",
                customHeaders = getHeader()
            )
        ).map { it.bodyAsText.parseToSocketMessage() }
    }

    //TODO : Gson() 이거 의존성 주입으로 수정
    private fun String.parseToSocketMessage(): SocketMessage {
        runCatching { Gson().fromJson(this, SocketMessage.CommonMessage::class.java) }
            .onSuccess { return it }
        runCatching { Gson().fromJson(this, SocketMessage.NotificationMessage::class.java) }
            .onSuccess { return it }
        throw JsonSyntaxException("Json cannot be deserialized to SocketMessage")
    }

    //이렇게 보내면 토큰 자동 갱신은 어케 하누?
    //TODO : Gson() 이거 의존성 주입으로 수정
    suspend fun sendMessage(
        stompSession: StompSession,
        roomId: Long,
        receiptId: Long,
        message: String
    ) {
        Log.d(TAG, "ChatRepository: sendMessage() - called")
        stompSession.send(
            StompSendHeaders(
                destination = "$SEND_MESSAGE_DESTINATION$roomId",
                customHeaders = getHeader()
            ), FrameBody.Text(Gson().toJson(RequestSendChat(receiptId, message)))
        )
    }

    private fun getHeader(): Map<String, String> {
        return mapOf(
            Pair(
                AUTHORIZATION,
                "${DataStoreManager.getBookChatTokenSync().getOrNull()?.accessToken}"
            )
        )
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val SEND_MESSAGE_DESTINATION = "/subscriptions/send/chatrooms/"
        private const val SUB_CHAT_ROOM_DESTINATION = "/topic/"
    }
}