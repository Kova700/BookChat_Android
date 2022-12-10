package com.example.bookchat

import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository() :UserRepository{
        return UserRepository()
    }

    @Provides
    @Singleton
    fun provideBookRepository(): BookRepository {
        return BookRepository()
    }
}