package com.kova700.bookchat.core.datastore.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

fun <T : Any> DataStore<Preferences>.getDataFlow(
	preferencesKey: Preferences.Key<T>,
): Flow<T?> {
	return data.catch { emit(emptyPreferences()) }
		.map { it[preferencesKey] }
}

suspend fun <T : Any> DataStore<Preferences>.setData(
	preferencesKey: Preferences.Key<T>,
	value: T
) {
	edit { preferences ->
		preferences[preferencesKey] = value
	}
}

suspend fun <T : Any> DataStore<Preferences>.clearData(
	preferencesKey: Preferences.Key<T>,
) {
	edit { preferences ->
		preferences.remove(preferencesKey)
	}
}