package com.example.bookchat.data.di

import com.example.bookchat.data.repository.AgonyRecordRepositoryImpl
import com.example.bookchat.data.repository.AgonyRepositoryImpl
import com.example.bookchat.data.repository.BookReportRepositoryImpl
import com.example.bookchat.data.repository.BookRepositoryImpl
import com.example.bookchat.data.repository.ChatRepositoryImpl
import com.example.bookchat.data.repository.ChatRoomManagementRepositoryImpl
import com.example.bookchat.data.repository.UserChatRoomRepositoryImpl
import com.example.bookchat.data.repository.ClientRepositoryImpl
import com.example.bookchat.data.repository.WholeChatRoomRepositoryImpl
import com.example.bookchat.domain.repository.AgonyRecordRepository
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.domain.repository.BookReportRepository
import com.example.bookchat.domain.repository.BookRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ChatRoomManagementRepository
import com.example.bookchat.domain.repository.UserChatRoomRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.WholeChatRoomRepository
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
	fun bindUserChatRoomRepository(
		repository: UserChatRoomRepositoryImpl
	): UserChatRoomRepository

	@Binds
	@Singleton
	fun bindChatRoomManagementRepository(
		repository: ChatRoomManagementRepositoryImpl
	): ChatRoomManagementRepository

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