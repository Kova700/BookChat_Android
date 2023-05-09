package com.example.bookchat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.bookchat.data.Chat
import com.example.bookchat.utils.UserDefaultProfileImageType

@Entity(
    tableName = "Chat", foreignKeys = [ForeignKey(
        entity = ChatRoomEntity::class,
        parentColumns = arrayOf("room_id"),
        childColumns = arrayOf("chat_room_id"),
        onDelete = ForeignKey.CASCADE,
    )]
)
data class ChatEntity(
    @PrimaryKey
    @ColumnInfo(name = "chat_id") val chatId: Long,
    @ColumnInfo(name = "chat_room_id") val chatRoomId: Long,
    @ColumnInfo(name = "sender_id") val senderId: Long?,
    @ColumnInfo(name = "sender_nickname") val senderNickname: String?,
    @ColumnInfo(name = "sender_profile_image_url") val senderProfileImageUrl: String?,
    @ColumnInfo(name = "sender_default_profile_image_type")
    val senderDefaultProfileImageType: UserDefaultProfileImageType?,
    @ColumnInfo(name = "dispatch_time") val dispatchTime: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "chat_type") val chatType: ChatType,
    @ColumnInfo(name = "status") val status: Int = ChatStatus.SUCCESS,
) {

    fun toChat() = Chat(
        chatId = this.chatId,
        senderId = this.senderId,
        senderNickname = this.senderNickname,
        senderProfileImageUrl = this.senderProfileImageUrl,
        senderDefaultProfileImageType = this.senderDefaultProfileImageType,
        dispatchTime = this.dispatchTime,
        message = this.message
    )

    enum class ChatType {
        Mine, Other, Notice
    }

    class ChatStatus {
        companion object {
            const val LOADING = -1
            const val FAIL = 0
            const val SUCCESS = 1
        }
    }
}