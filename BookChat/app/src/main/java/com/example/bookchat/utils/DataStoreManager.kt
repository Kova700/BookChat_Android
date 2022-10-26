package com.example.bookchat.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.bookchat.App
import com.example.bookchat.data.IdToken
import com.example.bookchat.data.Token
import com.example.bookchat.response.IdTokenDoseNotExistException
import com.example.bookchat.response.TokenDoseNotExistException
import com.example.bookchat.utils.Constants.TAG
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import java.io.IOException

//보관해야하는 값
//1. 북챗 토큰 (암호화 추가해야함)
//2. 검색 히스토리

object DataStoreManager {

    private const val DATASTORE_KEY = "DATASTORE_KEY"
    private const val ID_TOKEN_KEY = "ID_TOKEN_KEY"
    private const val BOOKCHAT_TOKEN_KEY = "BOOKCHAT_TOKEN_KEY"
    private const val SEARCH_HISTORY_KEY = "SEARCH_HISTORY_KEY"

    private val Context.dataStore by preferencesDataStore(DATASTORE_KEY)
    private val idTokenKey = stringPreferencesKey(ID_TOKEN_KEY)
    private val bookChatTokenKey = stringPreferencesKey(BOOKCHAT_TOKEN_KEY)
    private val searchHistoryKey = stringPreferencesKey(SEARCH_HISTORY_KEY)

    //북챗 토큰 (추후 암호화 추가)
    suspend fun getBookchatToken(): Token {
        val tokenString = readDataStore().firstOrNull()?.get(bookChatTokenKey)
        if (tokenString.isNullOrBlank()) throw TokenDoseNotExistException()
        val token = Gson().fromJson(tokenString, Token::class.java)
        Log.d(TAG, "DataStoreManager: getBookchatToken() - token : $token")
        return token
    }

    suspend fun saveBookchatToken(token: Token) {
        token.accessToken = "Bearer ${token.accessToken}"
        val tokenString = Gson().toJson(token)
        Log.d(TAG, "DataStoreManager: saveBookchatToken() - called")
        setDataStore(bookChatTokenKey, tokenString)
    }

    //ID토큰 (추후 암호화 추가)
    suspend fun getIdToken(): IdToken {
        val idTokenString = readDataStore().firstOrNull()?.get(idTokenKey)
        if (idTokenString.isNullOrBlank()) throw IdTokenDoseNotExistException()
        val idToken = Gson().fromJson(idTokenString, IdToken::class.java)
        Log.d(TAG, "DataStoreManager: getIdToken() - idToken : $idToken")
        return idToken
    }

    suspend fun saveIdToken(idToken: IdToken) {
        val idTokenString = Gson().toJson(idToken)
        setDataStore(idTokenKey, idTokenString)
    }

    suspend fun deleteBookchatToken() {
        removeDataStore(bookChatTokenKey)
    }

    suspend fun deleteIdToken() {
        removeDataStore(idTokenKey)
    }

    suspend fun <T : Any> removeDataStore(
        preferencesKey: Preferences.Key<T>,
    ) {
        App.instance.applicationContext.dataStore.edit { preferences ->
            preferences.remove(preferencesKey)
        }
    }

    suspend fun <T : Any> setDataStore(
        preferencesKey: Preferences.Key<T>,
        value: T
    ) {
        App.instance.applicationContext.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    private fun readDataStore(): Flow<Preferences> {
        return App.instance.applicationContext.dataStore.data
            .catch { exception -> if (exception is IOException) emit(emptyPreferences()) }
    }

}