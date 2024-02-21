package com.example.bookchat.data.di

import com.example.bookchat.data.repository.UserRepositoryImpl
import com.example.bookchat.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

	@Binds
	@Singleton
	fun bindUserRepository(repository: UserRepository): UserRepositoryImpl
}