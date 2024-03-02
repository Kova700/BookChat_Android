package com.example.bookchat.data.di

import com.example.bookchat.data.repository.AgonyRecordRepositoryImpl
import com.example.bookchat.data.repository.AgonyRepositoryImpl
import com.example.bookchat.data.repository.BookReportRepositoryImpl
import com.example.bookchat.data.repository.BookRepositoryImpl
import com.example.bookchat.data.repository.ChannelRepositoryImpl
import com.example.bookchat.data.repository.ChatRepositoryImpl
import com.example.bookchat.data.repository.ChattingRepositoryFacade
import com.example.bookchat.data.repository.ClientRepositoryImpl
import com.example.bookchat.data.repository.UserRepositoryImpl
import com.example.bookchat.data.repository.WholeChatRoomRepositoryImpl
import com.example.bookchat.domain.repository.AgonyRecordRepository
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.domain.repository.BookReportRepository
import com.example.bookchat.domain.repository.BookRepository
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.UserRepository
import com.example.bookchat.domain.repository.WholeChatRoomRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

	@Binds
	@Singleton
	fun bindClientRepository(
		repository: ClientRepositoryImpl
	): ClientRepository

	@Binds
	@Singleton
	fun bindBookRepository(
		repository: BookRepositoryImpl
	): BookRepository

	@Binds
	@Singleton
	fun bindBookReportRepository(
		repository: BookReportRepositoryImpl
	): BookReportRepository

	@Binds
	@Singleton
	fun bindAgonyRepository(
		repository: AgonyRepositoryImpl
	): AgonyRepository

	@Binds
	@Singleton
	fun bindAgonyRecordRepository(
		repository: AgonyRecordRepositoryImpl
	): AgonyRecordRepository

	@Binds
	@Singleton
	fun bindChannelRepository(
		repository: ChannelRepositoryImpl
	): ChannelRepository

	@Binds
	@Singleton
	fun bindUserRepository(
		repository: UserRepositoryImpl
	): UserRepository

	@Binds
	@Singleton
	fun bindChatRepository(
		repository: ChatRepositoryImpl
	): ChatRepository

	@Binds
	@Singleton
	fun bindWholeChatRoomRepository(
		repository: WholeChatRoomRepositoryImpl
	): WholeChatRoomRepository

}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule2 {

	@Provides
	@Singleton
	fun provideChattingRepositoryFacade(
		chatRepository: ChatRepository,
		channelRepository: ChannelRepository,
	): ChattingRepositoryFacade {
		return ChattingRepositoryFacade(
			chatRepository = chatRepository,
			channelRepository = channelRepository
		)
	}

}