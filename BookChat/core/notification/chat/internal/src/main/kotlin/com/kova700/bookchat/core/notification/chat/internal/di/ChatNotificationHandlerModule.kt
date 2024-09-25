package com.kova700.bookchat.core.notification.chat.internal.di

import com.kova700.bookchat.core.notification.chat.external.ChatNotificationHandler
import com.kova700.bookchat.core.notification.chat.internal.ChatNotificationHandlerImpl
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