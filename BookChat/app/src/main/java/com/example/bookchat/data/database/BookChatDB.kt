package com.example.bookchat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookchat.data.database.dao.ChannelDAO
import com.example.bookchat.data.database.dao.ChatDAO
import com.example.bookchat.data.database.dao.TempMessageDAO
import com.example.bookchat.data.database.dao.UserDAO
import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.TempMessageEntity
import com.example.bookchat.data.database.model.UserEntity
import com.example.bookchat.data.database.typeconverter.LongListTypeConverter
import com.example.bookchat.data.database.typeconverter.StringListTypeConverter

@Database(
	entities = [
		ChannelEntity::class, ChatEntity::class,
		UserEntity::class, TempMessageEntity::class
	],
	version = 1,
	exportSchema = false
)
@TypeConverters(value = [StringListTypeConverter::class, LongListTypeConverter::class])
abstract class BookChatDB : RoomDatabase() {
	abstract fun chatDAO(): ChatDAO
	abstract fun channelDAO(): ChannelDAO
	abstract fun userDAO(): UserDAO
	abstract fun tempMessageDAO(): TempMessageDAO
}