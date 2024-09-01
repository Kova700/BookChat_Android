package com.kova700.core.data.searchhistory.internal.di

import com.kova700.core.data.searchhistory.external.SearchHistoryRepository
import com.kova700.core.data.searchhistory.internal.SearchHistoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface SearchHistoryRepositoryModule {

	@Binds
	@Singleton
	fun bindSearchHistoryRepository(repository: SearchHistoryRepositoryImpl): SearchHistoryRepository
}