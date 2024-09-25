package com.kova700.bookchat.core.data.client.internal.di

import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.client.internal.ClientRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ClientRepositoryModule {
	@Binds
	@Singleton
	fun bindClientRepository(
		repository: ClientRepositoryImpl,
	): ClientRepository
}