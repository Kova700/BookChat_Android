package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private var bookRepository: BookRepository
) : ViewModel() {

    val searchTapStatus = MutableStateFlow<SearchTapStatus>(SearchTapStatus.Default)
    val searchKeyWord = MutableStateFlow<String>("")

    var simpleBooksearchResult = MutableStateFlow<List<Book>>(listOf())
    var previousKeyword = ""

    val bookResultState = MutableStateFlow<SearchState>(SearchState.Loading)
    val chatResultState = MutableStateFlow<SearchState>(SearchState.EmptyResult)

    val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            renewSearchTapStatus()
        }
    }

    private fun renewSearchTapStatus() {
        if (searchKeyWord.value.isEmpty()) return
        if (searchTapStatus.value != SearchTapStatus.Searching) searchTapStatus.value =
            SearchTapStatus.Searching
    }

    fun searchKeyword() = viewModelScope.launch {
        val keyword = searchKeyWord.value
        if (keyword.isEmpty()) {
            makeToast(R.string.search_book_keyword_empty)
            return@launch
        }
        if ((keyword == previousKeyword) && (searchTapStatus.value == SearchTapStatus.Result)) return@launch
        DataStoreManager.saveSearchHistory(searchKeyWord.value)
        simpleSearchBooks(keyword)
        simpleSearchChatRoom(keyword)
    }

    fun clickHistory(keyword: String) = viewModelScope.launch {
        searchKeyWord.value = keyword
        simpleSearchBooks(keyword)
        simpleSearchChatRoom(keyword)
    }

    val keyboardEnterListener = TextView.OnEditorActionListener { _, _, _ ->
        searchKeyword()
        false
    }

    private suspend fun simpleSearchBooks(keyword: String) {
        bookResultState.value = SearchState.Loading
        runCatching { bookRepository.simpleSearchBooks(keyword) }
            .onSuccess { respond -> searchBooksSuccessCallBack(respond, keyword) }
            .onFailure { failHandler(it) }
    }

    private fun searchBooksSuccessCallBack(respond: ResponseGetBookSearch, keyword: String) {
        simpleBooksearchResult.value = respond.bookResponses
        if (simpleBooksearchResult.value.isEmpty()){
            bookResultState.value = SearchState.EmptyResult
        }else{
            bookResultState.value = SearchState.HaveResult
        }
        previousKeyword = keyword
    }

    //채팅방 3개만 호출
    private suspend fun simpleSearchChatRoom(keyword: String) {
    }

    //상세페이지 이동시 호출
    private suspend fun detailSearchChatRoom(keyword: String) {
//        runCatching { bookRepository.searchChatRoom(keyword) }
    }

    fun clickBookDetailBtn() = viewModelScope.launch {
        searchTapStatus.value = SearchTapStatus.Detail
    }

    fun clickSearchWindow() {
        searchTapStatus.value = SearchTapStatus.History
    }

    fun clickBackBtn() {
        clearSearchWindow()
        searchTapStatus.value = SearchTapStatus.Default
    }

    fun clearSearchWindow() {
        searchKeyWord.value = ""
    }

    fun isStateLoading(searchState: SearchState) =
        searchState == SearchState.Loading
    fun isOnlyBookEmptyResult(bookSearchState: SearchState, chatSearchState: SearchState) =
        (bookSearchState == SearchState.EmptyResult) && (chatSearchState != SearchState.EmptyResult)
    fun isOnlyChatEmptyResult(bookSearchState: SearchState, chatSearchState: SearchState) =
        (bookSearchState != SearchState.EmptyResult) && (chatSearchState == SearchState.EmptyResult)
    fun isBothEmptyResult(bookSearchState: SearchState, chatSearchState: SearchState) =
        (bookSearchState == SearchState.EmptyResult) && (chatSearchState == SearchState.EmptyResult)

    sealed class SearchState {
        object Loading : SearchState()
        object HaveResult : SearchState()
        object EmptyResult : SearchState()
    }

    fun isSearchTapDefault(searchTapStatus :SearchTapStatus) =
        searchTapStatus == SearchTapStatus.Default
    fun isSearchTapSearching(searchTapStatus :SearchTapStatus) =
        searchTapStatus == SearchTapStatus.Searching

    sealed class SearchTapStatus {
        object Default : SearchTapStatus()
        object History : SearchTapStatus()
        object Searching : SearchTapStatus()
        object Result : SearchTapStatus()
        object Detail : SearchTapStatus()
    }

    private fun makeToast(stringId: Int) {
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }

    private fun failHandler(exception: Throwable) {
        when (exception) {
            is NetworkIsNotConnectedException ->
                makeToast(R.string.error_network)
        }
    }
}