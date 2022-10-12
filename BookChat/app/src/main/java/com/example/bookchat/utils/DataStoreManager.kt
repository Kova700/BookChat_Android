package com.example.bookchat.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bookchat.App
import kotlinx.coroutines.flow.*
import java.io.IOException

//보관해야하는 값
    //1. 어떤 종류의 토큰을 가지고 있는지
        // 둘다 가지고 있다면 최근에 로그인했던 종류의 토큰으로 로그인 진행
    //2. 검색 히스토리

object DataStoreManager {

    private val Context.dataStore  by preferencesDataStore(name = "DATASTORE")
    private val tokenKey = stringPreferencesKey("TOKEN_INFO")
    private val historyKey = stringPreferencesKey("Search_HISTORY")

    //로그아웃시에 토큰 타입 지우는 로직 추가해야함

    suspend fun getTokenType() :String?{
        val preferences = readDataStore()
        return preferences.firstOrNull()?.get(tokenKey)
    }

    suspend fun setTokenType(value: LoginType){
        setDataStore(tokenKey,value.name)
    }

    suspend fun <T : Any> setDataStore(
        preferencesKey: Preferences.Key<T>,
        value: T)
    {
        App.instance.applicationContext.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    private fun readDataStore(): Flow<Preferences> {
        return App.instance.applicationContext.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
    }

}