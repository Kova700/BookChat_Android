package com.kova700.bookchat.core.stomp.chatting.internal.di

import com.kova700.bookchat.core.stomp.chatting.external.StompHandler
import com.kova700.bookchat.core.stomp.chatting.internal.StompHandlerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface StompHandlerModule {
	@Binds
	@Singleton
	fun bindStompHandler(
		stompHandler: StompHandlerImpl,
	): StompHandler
}