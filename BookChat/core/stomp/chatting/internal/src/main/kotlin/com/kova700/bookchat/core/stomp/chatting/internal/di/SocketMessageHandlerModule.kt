package com.kova700.bookchat.core.stomp.chatting.internal.di

import com.kova700.bookchat.core.stomp.chatting.external.SocketMessageHandler
import com.kova700.bookchat.core.stomp.chatting.internal.SocketMessageHandlerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SocketMessageHandlerModule {
	@Binds
	@Singleton
	fun bindSocketMessageHandler(
		socketMessageHandler: SocketMessageHandlerImpl,
	): SocketMessageHandler
}