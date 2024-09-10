package com.kova700.bookchat.database.chatting.internal

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kova700.bookchat.core.database.chatting.external.channel.ChannelDAO
import com.kova700.bookchat.core.database.chatting.external.channel.model.ChannelEntity
import com.kova700.bookchat.core.database.chatting.external.chat.ChatDAO
import com.kova700.bookchat.core.database.chatting.external.chat.model.ChatEntity
import com.kova700.bookchat.core.database.chatting.external.tempmessage.TempMessageDAO
import com.kova700.bookchat.core.database.chatting.external.tempmessage.model.TempMessageEntity
import com.kova700.bookchat.core.database.chatting.external.user.UserDAO
import com.kova700.bookchat.core.database.chatting.external.user.model.UserEntity
import com.kova700.bookchat.database.chatting.internal.typeconverter.LongListTypeConverter
import com.kova700.bookchat.database.chatting.internal.typeconverter.ParticipantAuthoritiesTypeConverter
import com.kova700.bookchat.database.chatting.internal.typeconverter.StringListTypeConverter

@Database(
	entities = [
		ChannelEntity::class, ChatEntity::class,
		UserEntity::class, TempMessageEntity::class
	],
	version = 1,
	exportSchema = false
)
@TypeConverters(
	value = [
		StringListTypeConverter::class,
		LongListTypeConverter::class,
		ParticipantAuthoritiesTypeConverter::class,
	]
)
abstract class BookChatDB : RoomDatabase() {
	abstract fun chatDAO(): ChatDAO
	abstract fun channelDAO(): ChannelDAO
	abstract fun userDAO(): UserDAO
	abstract fun tempMessageDAO(): TempMessageDAO
}