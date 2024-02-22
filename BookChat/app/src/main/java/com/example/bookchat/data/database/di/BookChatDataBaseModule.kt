package com.example.bookchat.data.database.di

import android.content.Context
import androidx.room.Room
import com.example.bookchat.data.database.BookChatDB
import com.example.bookchat.data.database.typeconverter.LongListTypeConverter
import com.example.bookchat.data.database.typeconverter.StringListTypeConverter
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookChatDataBaseModule {
	private const val DB_NAME = "BookChat_DB"

	@Singleton
	@Provides
	fun provideBookChatDatabase(
		@ApplicationContext context: Context,
		gson: Gson
	): BookChatDB {
		return Room.databaseBuilder(context, BookChatDB::class.java, DB_NAME)
			.addTypeConverter(StringListTypeConverter(gson))
			.addTypeConverter(LongListTypeConverter(gson))
			.build()
	}

}