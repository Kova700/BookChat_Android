package com.kova700.bookchat.core.data.user.internal.di

import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.bookchat.core.data.user.internal.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface UserRepositoryModule {

	@Binds
	@Singleton
	fun bindUserRepository(repository: UserRepositoryImpl): UserRepository
}