package com.example.bookchat.notification.iconbuilder.di

import com.example.bookchat.notification.iconbuilder.IconBuilder
import com.example.bookchat.notification.iconbuilder.IconBuilderImpl
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