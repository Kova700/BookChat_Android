package com.example.bookchat.utils

import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.data.IdToken
import com.example.bookchat.data.Token
import com.example.bookchat.data.response.TokenDoseNotExistException
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.UUID

object DataStoreManager : BaseDataStoreManager() {
	private var cachedIdToken: IdToken? = null

	private const val TOKEN_TYPE = "Bearer"
	private const val BOOKCHAT_TOKEN_KEY = "BOOKCHAT_TOKEN_KEY"
	private const val SEARCH_HISTORY_KEY = "SEARCH_HISTORY_KEY"
	private const val FCM_TOKEN_KEY = "FCM_TOKEN_KEY"
	private const val DEVICE_UUID_KEY = "DEVICE_UUID_KEY"

	private val bookChatTokenKey = stringPreferencesKey(BOOKCHAT_TOKEN_KEY)
	private val searchHistoryKey = stringPreferencesKey(SEARCH_HISTORY_KEY)
	private val FCMTokenKey = stringPreferencesKey(FCM_TOKEN_KEY)
	private val deviceUUIDKey = stringPreferencesKey(DEVICE_UUID_KEY)

	fun getBookChatTokenSync() =
		runBlocking { runCatching { getBookChatToken() } }

	fun saveBookChatTokenSync(token: Token) =
		runBlocking { runCatching { saveBookChatToken(token) } }

	fun getFCMTokenSync() =
		runBlocking { runCatching { getFCMToken() } }

	fun saveFCMTokenSync(token: String) =
		runBlocking { runCatching { saveFCMToken(token) } }

	//북챗 토큰 (추후 암호화 추가)
	//Gson 의존성 주입으로 수정
	//북챗 토큰 없으면 로그아웃해야함(로컬 DB 삭제?)
	suspend fun getBookChatToken(): Token {
		val tokenString = getDataFlow().firstOrNull()?.get(bookChatTokenKey)
		if (tokenString.isNullOrBlank()) throw TokenDoseNotExistException()
		val token = Gson().fromJson(tokenString, Token::class.java)
		return token
	}

	fun isAccessTokenExist(): Boolean {
		return getBookChatTokenSync().getOrNull()?.accessToken.isNullOrBlank()
	}

	suspend fun saveBookChatToken(token: Token) {
		token.accessToken = "$TOKEN_TYPE ${token.accessToken}"
		val tokenString = Gson().toJson(token)
		setData(bookChatTokenKey, tokenString)
	}

	suspend fun getFCMToken(): String {
		val tokenString = getDataFlow().firstOrNull()?.get(FCMTokenKey)
		return tokenString ?: throw IOException("Saved FCMToken does not exist") //FCM 토큰 없으면 어떻게함?
	}

	suspend fun getDeviceID(): String {
		val deviceID = getDataFlow().firstOrNull()?.get(deviceUUIDKey)
		if (deviceID == null) {
			saveDeviceID()
			return getDataFlow().firstOrNull()?.get(deviceUUIDKey)
				?: throw IOException("Saved DeviceID does not exist")
		}
		return deviceID
	}

	private suspend fun saveDeviceID() {
		val uuid = UUID.randomUUID().toString()
		setData(deviceUUIDKey, uuid)
	}

	suspend fun saveFCMToken(token: String) {
		setData(FCMTokenKey, token)
	}

	fun getIdToken(): IdToken {
		return this.cachedIdToken ?: throw IOException("Saved IdToken does not exist")
	}

	fun saveIdToken(idToken: IdToken) {
		this.cachedIdToken = idToken
	}

	suspend fun getSearchHistory(): MutableList<String>? {
		val historyString = getDataFlow().firstOrNull()?.get(searchHistoryKey)
		if (historyString.isNullOrBlank()) return null
		val historyList = Gson().fromJson(historyString, Array<String>::class.java).toMutableList()
		return historyList
	}

	suspend fun saveSearchHistory(searchKeyWord: String) {
		val oldHistoryList = getSearchHistory()
		val newHistoryList = mutableListOf(searchKeyWord)
		newHistoryList.addAll(oldHistoryList ?: listOf())
		val historyString: String = Gson().toJson(newHistoryList)
		setData(searchHistoryKey, historyString)
	}

	suspend fun clearSearchHistory() {
		removeData(searchHistoryKey)
	}

	suspend fun overWriteHistory(historyList: List<String>) {
		val historyString: String = Gson().toJson(historyList)
		setData(searchHistoryKey, historyString)
	}

	suspend fun deleteBookChatToken() {
		removeData(bookChatTokenKey)
	}

	suspend fun deleteFCMToken() {
		removeData(FCMTokenKey)
	}
}