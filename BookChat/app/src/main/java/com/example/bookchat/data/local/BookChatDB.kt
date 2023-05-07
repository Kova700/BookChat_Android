package com.example.bookchat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bookchat.data.local.dao.ChatDAO
import com.example.bookchat.data.local.dao.ChatRoomDAO
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatRoomEntity

@Database(entities = [ChatRoomEntity::class, ChatEntity::class], version = 1, exportSchema = false)
abstract class BookChatDB : RoomDatabase() {
    abstract fun chatDAO(): ChatDAO
    abstract fun chatRoomDAO(): ChatRoomDAO

    companion object {
        private const val DB_NAME = "BookChat_DB"

        @Volatile
        private var instance: BookChatDB? = null

        fun getDatabase(context: Context): BookChatDB {
            return instance ?: synchronized(this) { buildDataBase(context) }
                .also { instance = it }
        }

        private fun buildDataBase(context: Context): BookChatDB {
            return Room.databaseBuilder(context, BookChatDB::class.java, DB_NAME).build()
        }
    }
}