package com.kova700.bookchat.core.data.agonyrecord.internal.di

import com.kova700.bookchat.core.data.agonyrecord.external.AgonyRecordRepository
import com.kova700.bookchat.core.data.agonyrecord.internal.AgonyRecordRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AgonyRecordRepositoryModule {
	@Binds
	@Singleton
	fun bindAgonyRecordRepository(
		repository: AgonyRecordRepositoryImpl,
	): AgonyRecordRepository
}