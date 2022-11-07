package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.LoadState
import com.example.bookchat.utils.SearchTapStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private var bookRepository :BookRepository) :ViewModel() {

    val _searchTapStatus = MutableStateFlow<SearchTapStatus>(SearchTapStatus.Default)
    val _searchKeyWord = MutableStateFlow<String>("")

    var simpleBooksearchResult : List<Book> = listOf()
    var bookSearchResultTotalItemCount = 0.toString()
    var previousKeyword = ""

    val resultLoadState = MutableStateFlow<LoadState>(LoadState.Default)
    val isSearchResultEmpty = MutableStateFlow<Boolean>(false)

    val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            renewSearchTapStatus()
        }
    }

    private fun renewSearchTapStatus(){
        if (_searchKeyWord.value.isEmpty()) {
            _searchTapStatus.value = SearchTapStatus.History
            return
        }
        _searchTapStatus.value = SearchTapStatus.Searching
    }

    fun searchKeyword() = viewModelScope.launch {
        val keyword = _searchKeyWord.value
        if (keyword.isEmpty()) {
            Toast.makeText(App.instance.applicationContext,"검색어를 입력해주세요.",Toast.LENGTH_SHORT).show() //임시
            return@launch
        }
        if (keyword == previousKeyword) return@launch
        simpleSearchBooks(keyword)
        simpleSearchChatRoom(keyword)
    }

    val keyboardEnterListener = TextView.OnEditorActionListener{ _, _, _ ->
        searchKeyword()
        false
    }

    //책 6개만 호출
    private suspend fun simpleSearchBooks(keyword :String){
        DataStoreManager.saveSearchHistory(_searchKeyWord.value)
        resultLoadState.value = LoadState.Loading
        runCatching { bookRepository.simpleSearchBooks(keyword) }
            .onSuccess { booksearchResult ->
                resultLoadState.value = LoadState.Result
                simpleBooksearchResult =  booksearchResult.bookResponses
                isSearchResultEmpty.value = simpleBooksearchResult.isEmpty()
                bookSearchResultTotalItemCount = booksearchResult.searchingMeta.totalCount.toString()
                previousKeyword = keyword
                _searchTapStatus.value = SearchTapStatus.Result
            }
            .onFailure { failHandler(it) }
    }

    //채팅방 3개만 호출
    private suspend fun simpleSearchChatRoom(keyword :String){
    }

    //상세페이지 이동시 호출
    private suspend fun detailSearchChatRoom(keyword :String){
//        runCatching { bookRepository.searchChatRoom(keyword) }
    }

    fun clickBookDetailBtn() = viewModelScope.launch{
        Log.d(TAG, "SearchViewModel: clickDetailBtn() - called")
        _searchTapStatus.value = SearchTapStatus.Detail
    }

    private fun failHandler(exception: Throwable){
        when(exception){
            is NetworkIsNotConnectedException -> //임시 토스트 처리
                Toast.makeText(App.instance.applicationContext, R.string.message_error_network, Toast.LENGTH_SHORT).show()
        }
    }

}