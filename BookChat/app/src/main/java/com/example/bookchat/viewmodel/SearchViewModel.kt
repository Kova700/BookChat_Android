package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.SearchTapStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(private var bookRepository :BookRepository) :ViewModel() {

    val _searchTapStatus = MutableStateFlow<SearchTapStatus>(SearchTapStatus.Default)
    //val searchTapStatus = _searchTapStatus.asStateFlow()

    val _searchKeyWord = MutableStateFlow<String>("")

//    lateinit var pagingSearchResultBook :Flow<PagingData<Book>>

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

    fun clickSearchbtn() = viewModelScope.launch {
        val keyword = _searchKeyWord.value
        if (keyword.isEmpty()) {
            Toast.makeText(App.instance.applicationContext,"검색어를 입력해주세요.",Toast.LENGTH_SHORT).show() //임시
            return@launch
        }
        _searchTapStatus.value = SearchTapStatus.Result
//        searchBooks(keyword) //페이징 시작
//        searchChatRoom(keyword) //페이징 시작
    }


    private suspend fun searchBooks(keyword :String) {
        //각자 책부분 채팅부분 로딩 프로그래스바 만들어서 서로 로딩상태 두개로 구분 해서 결과 기다리는 표시하기
//       runCatching { bookRepository.searchBooks(keyword) }
    }

    private suspend fun searchChatRoom(keyword :String){
//        runCatching { bookRepository.searchChatRoom(keyword) }
    }


}