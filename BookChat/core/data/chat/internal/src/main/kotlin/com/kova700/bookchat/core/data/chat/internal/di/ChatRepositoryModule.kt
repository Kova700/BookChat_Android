package com.kova700.bookchat.core.data.chat.internal.di

import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.chat.internal.ChatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface ChatRepositoryModule {

	@Binds
	@Singleton
	fun bindChatRepository(repository: ChatRepositoryImpl): ChatRepository
}