package com.example.bookchat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookchat.data.local.dao.ChatDAO
import com.example.bookchat.data.local.dao.ChatRoomDAO
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.data.local.typeconverter.LongListTypeConverter
import com.example.bookchat.data.local.typeconverter.StringListTypeConverter
import com.google.gson.Gson

@Database(entities = [ChatRoomEntity::class, ChatEntity::class], version = 1, exportSchema = false)
@TypeConverters(value = [StringListTypeConverter::class, LongListTypeConverter::class])
abstract class BookChatDB : RoomDatabase() {
    abstract fun chatDAO(): ChatDAO
    abstract fun chatRoomDAO(): ChatRoomDAO

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