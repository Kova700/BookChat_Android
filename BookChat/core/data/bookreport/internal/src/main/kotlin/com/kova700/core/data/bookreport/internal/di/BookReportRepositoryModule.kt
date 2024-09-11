package com.kova700.core.data.bookreport.internal.di

import com.kova700.core.data.bookreport.external.BookReportRepository
import com.kova700.core.data.bookreport.internal.BookReportRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BookReportRepositoryModule {
	@Binds
	@Singleton
	fun bindBookReportRepository(
		repository: BookReportRepositoryImpl,
	): BookReportRepository
}