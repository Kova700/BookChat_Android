package com.kova700.bookchat.core.data.agony.internal.di

import com.kova700.bookchat.core.data.agony.external.AgonyRepository
import com.kova700.bookchat.core.data.agony.internal.AgonyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AgonyRepositoryModule {
	@Binds
	@Singleton
	fun bindAgonyRepository(
		repository: AgonyRepositoryImpl,
	): AgonyRepository
}