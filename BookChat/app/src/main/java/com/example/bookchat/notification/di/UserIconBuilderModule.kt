package com.example.bookchat.notification.di

import com.example.bookchat.notification.UserIconBuilder
import com.example.bookchat.notification.UserIconBuilderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface UserIconBuilderModule {
	@Binds
	@Singleton
	fun bindUserIconBuilder(
		userIconBuilder: UserIconBuilderImpl,
	): UserIconBuilder
}