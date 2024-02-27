package com.example.bookchat.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookchat.data.database.dao.ChatDAO
import com.example.bookchat.data.database.dao.ChannelDAO
import com.example.bookchat.data.database.dao.TempMessageDAO
import com.example.bookchat.data.database.dao.UserDAO
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.data.database.model.TempMessageEntity
import com.example.bookchat.data.database.model.UserEntity
import com.example.bookchat.data.database.typeconverter.LongListTypeConverter
import com.example.bookchat.data.database.typeconverter.StringListTypeConverter
import com.google.gson.Gson

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

    companion object {
        private const val DB_NAME = "BookChat_DB"

        @Volatile
        private var instance: BookChatDB? = null

        fun getDatabase(context: Context): BookChatDB {
            return instance ?: synchronized(this) { buildDataBase(context, Gson()) }
                .also { instance = it }
        }

        private fun buildDataBase(context: Context, jsonParser: Gson): BookChatDB {
            return Room.databaseBuilder(context, BookChatDB::class.java, DB_NAME)
                .addTypeConverter(StringListTypeConverter(jsonParser))
                .addTypeConverter(LongListTypeConverter(jsonParser))
                .build()
        }
    }
}