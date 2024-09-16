package com.kova700.bookchat.core.network.bookchat.bookreport.di

import com.kova700.bookchat.core.network.bookchat.bookreport.BookReportApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class BookReportApiModule {

	@Provides
	fun provideBookReportApi(
		retrofit: Retrofit,
	): BookReportApi {
		return retrofit.create(BookReportApi::class.java)
	}
}