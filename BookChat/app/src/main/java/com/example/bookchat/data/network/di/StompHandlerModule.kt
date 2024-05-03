package com.example.bookchat.data.network.di

import com.example.bookchat.data.repository.StompHandlerImpl
import com.example.bookchat.domain.repository.StompHandler
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