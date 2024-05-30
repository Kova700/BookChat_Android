package com.example.bookchat.data.database.di

import android.content.Context
import androidx.room.Room
import com.example.bookchat.data.database.BookChatDB
import com.example.bookchat.data.database.dao.ChannelDAO
import com.example.bookchat.data.database.dao.ChatDAO
import com.example.bookchat.data.database.dao.TempMessageDAO
import com.example.bookchat.data.database.dao.UserDAO
import com.example.bookchat.data.database.typeconverter.LongListTypeConverter
import com.example.bookchat.data.database.typeconverter.ParticipantAuthoritiesTypeConverter
import com.example.bookchat.data.database.typeconverter.StringListTypeConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookChatDBModule {
	private const val DB_NAME = "BookChat_DB"

	@Provides
	@Singleton
	fun provideBookChatDatabase(
		@ApplicationContext context: Context,
		stringListTypeConverter: StringListTypeConverter,
		longListTypeConverter: LongListTypeConverter,
		participantAuthoritiesTypeConverter: ParticipantAuthoritiesTypeConverter,
	): BookChatDB {
		return Room.databaseBuilder(context, BookChatDB::class.java, DB_NAME)
			.addTypeConverter(stringListTypeConverter)
			.addTypeConverter(longListTypeConverter)
			.addTypeConverter(participantAuthoritiesTypeConverter)
			.build()
	}

	@Provides
	@Singleton
	fun provideUserDAO(bookChatDB: BookChatDB): UserDAO {
		return bookChatDB.userDAO()
	}

	@Provides
	@Singleton
	fun provideChatRoomDAO(bookChatDB: BookChatDB): ChannelDAO {
		return bookChatDB.channelDAO()
	}

	@Provides
	@Singleton
	fun provideChatDAO(bookChatDB: BookChatDB): ChatDAO {
		return bookChatDB.chatDAO()
	}

	@Provides
	@Singleton
	fun provideTempMessageDAO(bookChatDB: BookChatDB): TempMessageDAO {
		return bookChatDB.tempMessageDAO()
	}

}