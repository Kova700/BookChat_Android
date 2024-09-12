package com.kova700.bookchat.core.data.bookshelf.internal.di

import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.bookshelf.internal.BookShelfRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BookShelfRepositoryModule {
	@Binds
	@Singleton
	fun bindBookShelfRepository(
		repository: BookShelfRepositoryImpl,
	): BookShelfRepository
}