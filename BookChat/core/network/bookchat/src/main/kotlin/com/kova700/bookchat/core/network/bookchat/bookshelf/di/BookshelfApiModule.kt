package com.kova700.bookchat.core.network.bookchat.bookshelf.di

import com.kova700.bookchat.core.network.bookchat.bookshelf.BookshelfApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class BookshelfApiModule {

	@Provides
	fun provideBookshelfApi(
		retrofit: Retrofit,
	): BookshelfApi {
		return retrofit.create(BookshelfApi::class.java)
	}
}