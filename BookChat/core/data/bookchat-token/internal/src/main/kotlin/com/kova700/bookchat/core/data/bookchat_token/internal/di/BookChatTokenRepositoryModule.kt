package com.kova700.bookchat.core.data.bookchat_token.internal.di

import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.bookchat_token.internal.BookChatTokenRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface BookChatTokenRepositoryModule {

	@Binds
	@Singleton
	fun bindChattingNotificationInfoRepository(
		repository: BookChatTokenRepositoryImpl,
	): BookChatTokenRepository
}