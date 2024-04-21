package com.example.bookchat.data.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal object DataStoreModule {
	private const val DATASTORE_KEY = "DATASTORE_KEY"
	private val Context.dataStore by preferencesDataStore(DATASTORE_KEY)

	@Singleton
	@Provides
	fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
		return appContext.dataStore
	}

}