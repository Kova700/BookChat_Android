package com.kova700.bookchat.core.network.bookchat.search.di

import com.kova700.bookchat.core.network.bookchat.search.SearchApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class SearchApiModule {

	@Provides
	fun provideSearchApi(
		retrofit: Retrofit,
	): SearchApi {
		return retrofit.create(SearchApi::class.java)
	}
}