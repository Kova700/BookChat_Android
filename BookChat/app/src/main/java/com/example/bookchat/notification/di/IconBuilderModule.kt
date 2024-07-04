package com.example.bookchat.notification.di

import com.example.bookchat.notification.IconBuilder
import com.example.bookchat.notification.IconBuilderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface IconBuilderModule {
	@Binds
	@Singleton
	fun bindIconBuilder(
		iconBuilder: IconBuilderImpl,
	): IconBuilder
}