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
import com.example.bookchat.data.WholeChatRoomListItem
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.data.response.ResponseGetWholeChatRoomList
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.WholeChatRoomRepository
import com.example.bookchat.utils.ChatSearchFilter
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.SearchPurpose
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.Serializable

class SearchViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    private val wholeChatRoomRepository: WholeChatRoomRepository,
    @Assisted val searchPurpose: SearchPurpose
) : ViewModel() {

    val searchTapStatus = MutableStateFlow<SearchTapStatus>(SearchTapStatus.Default)
    val searchKeyWord = MutableStateFlow<String>("")

    var simpleBookSearchResult = MutableStateFlow<List<Book>>(listOf())
    var simpleChatRoomSearchResult = MutableStateFlow<List<WholeChatRoomListItem>>(listOf())
    var previousSearchKeyword = ""

    val bookResultState = MutableStateFlow<SearchState>(SearchState.Loading)
    val chatResultState = MutableStateFlow<SearchState>(SearchState.Loading)
    val chatSearchFilter = MutableStateFlow<ChatSearchFilter>(ChatSearchFilter.ROOM_NAME)

    val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            renewSearchTapStatus()
        }
    }

    private fun renewSearchTapStatus() {
        if (searchKeyWord.value.isEmpty()) return //다시 확인
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
        requestSearchApi(keyword)
    }

    fun clickHistory(keyword: String) = viewModelScope.launch {
        searchKeyWord.value = keyword
        while (searchTapStatus.value != SearchTapStatus.Searching) delay(WAITING_DURATION)
        requestSearchApi(keyword)
    }

    private fun isSameSearchKeyword(keyword: String) =
        (keyword == previousSearchKeyword) && (searchTapStatus.value == SearchTapStatus.Result)

    private fun requestSearchApi(keyword: String) {
        when (searchPurpose) {
            is SearchPurpose.DefaultSearch -> {
                simpleSearchBooks(keyword)
                simpleSearchChatRoom(keyword)
            }
            is SearchPurpose.MakeChatRoom -> {
                simpleSearchBooks(keyword)
            }
            is SearchPurpose.SearchChatRoom -> {
                simpleSearchChatRoom(keyword)
            }
        }
    }

    val keyboardEnterListener = TextView.OnEditorActionListener { _, _, _ ->
        searchKeyword()
        false
    }

    private fun simpleSearchBooks(keyword: String) = viewModelScope.launch {
        bookResultState.value = SearchState.Loading
        searchTapStatus.value = SearchTapStatus.Result
        runCatching { bookRepository.simpleSearchBooks(keyword) }
            .onSuccess { searchBooksSuccessCallBack(it, keyword) }
            .onFailure { failHandler(it) }
    }

    private suspend fun searchBooksSuccessCallBack(
        respond: ResponseGetBookSearch,
        keyword: String
    ) {
        delay(SKELETON_DURATION)
        simpleBookSearchResult.value = respond.bookResponses
        previousSearchKeyword = keyword
        if (simpleBookSearchResult.value.isEmpty()) {
            bookResultState.value = SearchState.EmptyResult
            return
        }
        bookResultState.value = SearchState.HaveResult
    }

    private fun simpleSearchChatRoom(keyword: String) = viewModelScope.launch {
        chatResultState.value = SearchState.Loading
        searchTapStatus.value = SearchTapStatus.Result
        runCatching { wholeChatRoomRepository.getWholeChatRoomList(keyword, chatSearchFilter.value) }
            .onSuccess { searchChatRoomsSuccessCallBack(it, keyword) }
            .onFailure { failHandler(it) }
    }

    private suspend fun searchChatRoomsSuccessCallBack(
        respond: ResponseGetWholeChatRoomList,
        keyword: String
    ) {
        delay(SKELETON_DURATION)
        //TestPagingDataSource 부분 채팅방 검색 API 수정시 삭제 예정
//        simpleChatRoomSearchResult.value = TestPagingDataSource.getSearchChatRoomData().chatRoomList
        simpleChatRoomSearchResult.value = respond.chatRoomList
        previousSearchKeyword = keyword
        if (simpleChatRoomSearchResult.value.isEmpty()) {
            chatResultState.value = SearchState.EmptyResult
            return
        }
        chatResultState.value = SearchState.HaveResult
    }

    fun clickBookDetailBtn() {
        searchTapStatus.value = SearchTapStatus.Detail(NecessaryDataFlagInDetail.Book)
    }

    fun clickChatRoomDetailBtn() {
        searchTapStatus.value = SearchTapStatus.Detail(NecessaryDataFlagInDetail.ChatRoom)
    }

    fun clickSearchWindow() {
        searchTapStatus.value = SearchTapStatus.History
    }

    fun clickBackBtn() {
        clearSearchWindow()
    }

    fun clearSearchWindow() {
        searchKeyWord.value = ""
        searchTapStatus.value = SearchTapStatus.Default
    }

    private fun isDefaultSearchPurpose() =
        searchPurpose == SearchPurpose.DefaultSearch

    private fun isMakeChatRoomPurpose() =
        searchPurpose == SearchPurpose.MakeChatRoom

    private fun isSearchChatRoomPurpose() =
        searchPurpose == SearchPurpose.SearchChatRoom

    private fun isStateLoading(searchState: SearchState) =
        searchState == SearchState.Loading

    private fun isStateHaveResult(searchState: SearchState) =
        searchState == SearchState.HaveResult

    private fun isOnlyBookEmptyResult(bookSearchState: SearchState, chatSearchState: SearchState) =
        (bookSearchState == SearchState.EmptyResult) && (chatSearchState != SearchState.EmptyResult)

    private fun isOnlyChatEmptyResult(bookSearchState: SearchState, chatSearchState: SearchState) =
        (bookSearchState != SearchState.EmptyResult) && (chatSearchState == SearchState.EmptyResult)

    fun isBothEmptyResult(bookSearchState: SearchState, chatSearchState: SearchState) =
        (bookSearchState == SearchState.EmptyResult) && (chatSearchState == SearchState.EmptyResult)

    fun isVisibleBookResultHeader(bookSearchState: SearchState, chatSearchState: SearchState) =
        !isSearchChatRoomPurpose() && !isBothEmptyResult(bookSearchState, chatSearchState)
                && !isStateLoading(bookSearchState)

    fun isVisibleBookRcv(bookSearchState: SearchState) =
        !isSearchChatRoomPurpose() && isStateHaveResult(bookSearchState)

    fun isVisibleBookEmptyResultLayout(bookSearchState: SearchState, chatSearchState: SearchState) =
        !isSearchChatRoomPurpose() && isOnlyBookEmptyResult(bookSearchState, chatSearchState)

    fun isVisibleChatRoomResultHeader(bookSearchState: SearchState, chatSearchState: SearchState) =
        !isMakeChatRoomPurpose() && !isBothEmptyResult(bookSearchState, chatSearchState)
                && !isStateLoading(chatSearchState)

    fun isVisibleChatRoomRcv(chatSearchState: SearchState) =
        !isMakeChatRoomPurpose() && isStateHaveResult(chatSearchState)

    fun isVisibleChatRoomEmptyResultLayout(
        bookSearchState: SearchState,
        chatSearchState: SearchState
    ) =
        !isMakeChatRoomPurpose() && isOnlyChatEmptyResult(bookSearchState, chatSearchState)

    fun isVisibleBookSkeleton(bookSearchState: SearchState) =
        !isSearchChatRoomPurpose() && isStateLoading(bookSearchState)

    fun isVisibleChatRoomSkeleton(chatSearchState: SearchState) =
        !isMakeChatRoomPurpose() && isStateLoading(chatSearchState)

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
        data class Detail(val necessaryDataFlag: NecessaryDataFlagInDetail) : SearchTapStatus()
    }

    sealed class NecessaryDataFlagInDetail : Serializable {
        object Book : NecessaryDataFlagInDetail()
        object ChatRoom : NecessaryDataFlagInDetail()
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
        private const val WAITING_DURATION = 200L
        private const val SKELETON_DURATION = 700L

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