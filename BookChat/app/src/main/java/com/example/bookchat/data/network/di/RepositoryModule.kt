package com.example.bookchat.data.network.di

import com.example.bookchat.data.repository.AgonyRecordRepositoryImpl
import com.example.bookchat.data.repository.AgonyRepositoryImpl
import com.example.bookchat.data.repository.BookChatTokenRepositoryIml
import com.example.bookchat.data.repository.BookReportRepositoryImpl
import com.example.bookchat.data.repository.BookSearchRepositoryImpl
import com.example.bookchat.data.repository.BookShelfRepositoryImpl
import com.example.bookchat.data.repository.ChannelRepositoryImpl
import com.example.bookchat.data.repository.ChannelSearchRepositoryImpl
import com.example.bookchat.data.repository.ChatRepositoryImpl
import com.example.bookchat.data.repository.ClientRepositoryImpl
import com.example.bookchat.data.repository.SearchHistoryRepositoryImpl
import com.example.bookchat.data.repository.UserRepositoryImpl
import com.example.bookchat.domain.repository.AgonyRecordRepository
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.BookReportRepository
import com.example.bookchat.domain.repository.BookSearchRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChannelSearchRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.SearchHistoryRepository
import com.example.bookchat.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
	@Binds
	@Singleton
	fun bindBookChatTokenRepository(
		repository: BookChatTokenRepositoryIml,
	): BookChatTokenRepository

	@Binds
	@Singleton
	fun bindClientRepository(
		repository: ClientRepositoryImpl,
	): ClientRepository

	@Binds
	@Singleton
	fun bindBookShelfRepository(
		repository: BookShelfRepositoryImpl,
	): BookShelfRepository

	@Binds
	@Singleton
	fun bindBookSearchRepository(
		repository: BookSearchRepositoryImpl,
	): BookSearchRepository

	@Binds
	@Singleton
	fun bindBookReportRepository(
		repository: BookReportRepositoryImpl,
	): BookReportRepository

	@Binds
	@Singleton
	fun bindAgonyRepository(
		repository: AgonyRepositoryImpl,
	): AgonyRepository

	@Binds
	@Singleton
	fun bindAgonyRecordRepository(
		repository: AgonyRecordRepositoryImpl,
	): AgonyRecordRepository

	@Binds
	@Singleton
	fun bindChannelRepository(
		repository: ChannelRepositoryImpl,
	): ChannelRepository

	@Binds
	@Singleton
	fun bindUserRepository(
		repository: UserRepositoryImpl,
	): UserRepository

	@Binds
	@Singleton
	fun bindChatRepository(
		repository: ChatRepositoryImpl,
	): ChatRepository

	@Binds
	@Singleton
	fun bindChannelSearchRepository(
		repository: ChannelSearchRepositoryImpl,
	): ChannelSearchRepository

	@Binds
	@Singleton
	fun bindSearchHistoryRepository(
		repository: SearchHistoryRepositoryImpl,
	): SearchHistoryRepository
}