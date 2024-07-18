package com.example.bookchat.notification.chat.di

import com.example.bookchat.notification.chat.ChatNotificationHandlerImpl
import com.example.bookchat.notification.chat.ChatNotificationHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ChatNotificationHandlerModule {
	@Binds
	@Singleton
	fun bindNotificationHandler(
		chatNotificationHandler: ChatNotificationHandlerImpl,
	): ChatNotificationHandler
}