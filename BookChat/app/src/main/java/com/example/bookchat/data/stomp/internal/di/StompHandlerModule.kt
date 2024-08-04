package com.example.bookchat.data.stomp.internal.di

import com.example.bookchat.data.stomp.external.StompHandler
import com.example.bookchat.data.stomp.internal.StompHandlerImpl
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
		stompHandler: StompHandlerImpl
	): StompHandler
}