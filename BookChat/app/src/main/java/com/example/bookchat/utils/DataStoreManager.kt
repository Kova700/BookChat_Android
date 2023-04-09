package com.example.bookchat.utils

import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.data.IdToken
import com.example.bookchat.data.Token
import com.example.bookchat.data.response.IdTokenDoseNotExistException
import com.example.bookchat.data.response.TokenDoseNotExistException
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

object DataStoreManager : BaseDataStoreManager(){
    private var cachedIdToken : IdToken? = null

    private const val TOKEN_TYPE = "Bearer"
    private const val BOOKCHAT_TOKEN_KEY = "BOOKCHAT_TOKEN_KEY"
    private const val SEARCH_HISTORY_KEY = "SEARCH_HISTORY_KEY"

    private val bookChatTokenKey = stringPreferencesKey(BOOKCHAT_TOKEN_KEY)
    private val searchHistoryKey = stringPreferencesKey(SEARCH_HISTORY_KEY)

    fun getBookChatTokenSync() =
        runBlocking { runCatching { getBookChatToken() } }
    fun saveBookChatTokenSync(token: Token) =
        runBlocking { runCatching { saveBookChatToken(token) } }

    //북챗 토큰 (추후 암호화 추가)
    suspend fun getBookChatToken(): Token {
        val tokenString = getDataFlow().firstOrNull()?.get(bookChatTokenKey)
        if (tokenString.isNullOrBlank()) throw TokenDoseNotExistException()
        val token = Gson().fromJson(tokenString, Token::class.java)
        return token
    }
    suspend fun saveBookChatToken(token: Token) {
        token.accessToken = "$TOKEN_TYPE ${token.accessToken}"
        val tokenString = Gson().toJson(token)
        setData(bookChatTokenKey, tokenString)
    }

    fun getIdToken(): IdToken {
        return this.cachedIdToken ?: throw IdTokenDoseNotExistException()
    }
    fun saveIdToken(idToken: IdToken) {
        this.cachedIdToken = idToken
    }

    suspend fun getSearchHistory() :MutableList<String>? {
        val historyString = getDataFlow().firstOrNull()?.get(searchHistoryKey)
        if (historyString.isNullOrBlank()) return null
        val historyList = Gson().fromJson(historyString, Array<String>::class.java).toMutableList()
        return historyList
    }

    suspend fun saveSearchHistory(searchKeyWord : String){
        val oldHistoryList = getSearchHistory()
        val newHistoryList =  mutableListOf(searchKeyWord)
        newHistoryList.addAll(oldHistoryList ?: listOf())
        val historyString : String = Gson().toJson(newHistoryList)
        setData(searchHistoryKey,historyString)
    }

    suspend fun clearSearchHistory(){
        removeData(searchHistoryKey)
    }

    suspend fun overWriteHistory(historyList : List<String>){
        val historyString : String = Gson().toJson(historyList)
        setData(searchHistoryKey,historyString)
    }

    suspend fun deleteBookChatToken() {
        removeData(bookChatTokenKey)
    }
}