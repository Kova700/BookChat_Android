package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.data.SearchChatRoomListItem
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.data.response.ResponseGetSearchChatRoomList
import com.example.bookchat.paging.TestPagingDataSource
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.ChatRoomRepository
import com.example.bookchat.utils.ChatSearchFilter
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.SearchPurpose
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SearchViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    private val chatRoomRepository: ChatRoomRepository,
    @Assisted val searchPurpose: SearchPurpose
) : ViewModel() {

    val searchTapStatus = MutableStateFlow<SearchTapStatus>(SearchTapStatus.Default)
    val searchKeyWord = MutableStateFlow<String>("")

    var simpleBookSearchResult = MutableStateFlow<List<Book>>(listOf())
    var simpleChatRoomSearchResult = MutableStateFlow<List<SearchChatRoomListItem>>(listOf())
    var previousSearchKeyword = ""

    val bookResultState = MutableStateFlow<SearchState>(SearchState.Loading)
    val chatResultState = MutableStateFlow<SearchState>(SearchState.Loading)
    val chatSearchFilter = MutableStateFlow<ChatSearchFilter>(ChatSearchFilter.BOOK_TITLE)

    val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            renewSearchTapStatus()
        }
    }

    private fun renewSearchTapStatus() {
        if (searchKeyWord.value.isEmpty()) return
        if (searchTapStatus.value != SearchTapStatus.Searching) {
            searchTapStatus.value = SearchTapStatus.Searching
        }
    }

    fun searchKeyword() = viewModelScope.launch {
        searchKeyWord.value = searchKeyWord.value.trim()
        val keyword = searchKeyWord.value
        if (keyword.isBlank()) {
            makeToast(R.string.search_book_keyword_empty)
            return@launch
        }
        if (isSameSearchKeyword(keyword)) return@launch
        DataStoreManager.saveSearchHistory(keyword)
        simpleSearchBooks(keyword)
        simpleSearchChatRoom(keyword)
    }

    private fun isSameSearchKeyword(keyword: String) =
        (keyword == previousSearchKeyword) && (searchTapStatus.value == SearchTapStatus.Result)

    fun clickHistory(keyword: String) = viewModelScope.launch {
        searchKeyWord.value = keyword
        while (searchTapStatus.value != SearchTapStatus.Searching) delay(200)
        simpleSearchBooks(keyword)
        simpleSearchChatRoom(keyword)
    }

    val keyboardEnterListener = TextView.OnEditorActionListener { _, _, _ ->
        searchKeyword()
        false
    }

    private suspend fun simpleSearchBooks(keyword: String) {
        bookResultState.value = SearchState.Loading
        searchTapStatus.value = SearchTapStatus.Result
        runCatching { bookRepository.simpleSearchBooks(keyword) }
            .onSuccess { searchBooksSuccessCallBack(it, keyword) }
            .onFailure { failHandler(it) }
    }

    private fun searchBooksSuccessCallBack(respond: ResponseGetBookSearch, keyword: String) {
        simpleBookSearchResult.value = respond.bookResponses
        previousSearchKeyword = keyword
        if (simpleBookSearchResult.value.isEmpty()) {
            bookResultState.value = SearchState.EmptyResult
            return
        }
        bookResultState.value = SearchState.HaveResult
    }

    private suspend fun simpleSearchChatRoom(keyword: String) {
        chatResultState.value = SearchState.Loading
        searchTapStatus.value = SearchTapStatus.Result
        runCatching { chatRoomRepository.simpleSearchChatRooms(keyword, chatSearchFilter.value) }
            .onSuccess { searchChatRoomsSuccessCallBack(it, keyword) }
            .onFailure { failHandler(it) }
    }

    private fun searchChatRoomsSuccessCallBack(
        respond: ResponseGetSearchChatRoomList,
        keyword: String
    ) {
        simpleChatRoomSearchResult.value = TestPagingDataSource.getSearchChatRoomData().chatRoomList
//        simpleChatRoomSearchResult.value = respond.chatRoomList
        previousSearchKeyword = keyword
        if (simpleChatRoomSearchResult.value.isEmpty()) {
            chatResultState.value = SearchState.EmptyResult
            return
        }
        chatResultState.value = SearchState.HaveResult
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

    fun isStateHaveResult(searchState: SearchState) =
        (searchState == SearchState.HaveResult)

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

    fun isSearchTapDefault(searchTapStatus: SearchTapStatus) =
        searchTapStatus == SearchTapStatus.Default

    fun isSearchTapDefaultOrHistory(searchTapStatus: SearchTapStatus) =
        (searchTapStatus == SearchTapStatus.Default) || (searchTapStatus == SearchTapStatus.History)

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
            else -> makeToast(R.string.error_else)

        }
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(searchPurpose: SearchPurpose): SearchViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            searchPurpose: SearchPurpose
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(searchPurpose) as T
            }
        }
    }
}