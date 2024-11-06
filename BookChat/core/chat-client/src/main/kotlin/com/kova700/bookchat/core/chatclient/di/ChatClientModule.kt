package com.kova700.bookchat.core.chatclient.di

import com.kova700.bookchat.core.chatclient.ChatClient
import com.kova700.bookchat.core.chatclient.ChatClientImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ChatClientModule {

	@Binds
	@Singleton
	fun bindChatClient(
		chatClient: ChatClientImpl,
	): ChatClient
}