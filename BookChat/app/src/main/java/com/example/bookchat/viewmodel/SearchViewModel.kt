package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.response.TokenExpiredException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.SearchTapStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(private var bookRepository :BookRepository) :ViewModel() {

    val _searchTapStatus = MutableStateFlow<SearchTapStatus>(SearchTapStatus.Default)

    val _searchKeyWord = MutableStateFlow<String>("")

    var simpleBooksearchResult : List<Book> = listOf()
    var bookSearchResultTotalItemCount = 0.toString()
    var previousKeyword = ""

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
        runCatching { bookRepository.simpleSearchBooks(keyword) }
            .onSuccess { booksearchResult ->
                simpleBooksearchResult =  booksearchResult.bookResponses
                bookSearchResultTotalItemCount = booksearchResult.meta.totalCount.toString()
                previousKeyword = keyword
                _searchTapStatus.value = SearchTapStatus.Result
            }
            .onFailure {  }
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

}