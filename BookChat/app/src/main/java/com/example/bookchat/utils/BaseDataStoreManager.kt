package com.example.bookchat.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.bookchat.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

open class BaseDataStoreManager {
    private val Context.dataStore by preferencesDataStore(DATASTORE_KEY)

    suspend fun <T : Any> removeData(
        preferencesKey: Preferences.Key<T>,
    ) {
        App.instance.applicationContext.dataStore.edit { preferences ->
            preferences.remove(preferencesKey)
        }
    }

    suspend fun <T : Any> setData(
        preferencesKey: Preferences.Key<T>,
        value: T
    ) {
        App.instance.applicationContext.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    fun getDataFlow(): Flow<Preferences> {
        return App.instance.applicationContext.dataStore.data
            .catch { exception -> if (exception is IOException) emit(emptyPreferences()) }
    }

    companion object{
        private const val DATASTORE_KEY = "DATASTORE_KEY"
    }
}