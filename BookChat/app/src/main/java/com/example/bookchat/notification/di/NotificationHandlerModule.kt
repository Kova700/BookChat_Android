package com.example.bookchat.notification.di

import com.example.bookchat.notification.ChatNotificationHandler
import com.example.bookchat.notification.NotificationHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NotificationHandlerModule {
	@Binds
	@Singleton
	fun bindNotificationHandler(
		notificationHandler: ChatNotificationHandler,
	): NotificationHandler
}