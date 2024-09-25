package com.kova700.bookchat.core.data.search.book.internal.di

import com.kova700.bookchat.core.data.search.book.external.BookSearchRepository
import com.kova700.bookchat.core.data.search.book.internal.BookSearchRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BookSearchRepositoryModule {
	@Binds
	@Singleton
	fun bindBookSearchRepository(
		repository: BookSearchRepositoryImpl,
	): BookSearchRepository
}