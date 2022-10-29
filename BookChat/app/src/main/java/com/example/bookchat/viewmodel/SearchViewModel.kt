package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModel
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.SearchTapStatus
import kotlinx.coroutines.flow.*

class SearchViewModel(private var bookRepository :BookRepository) :ViewModel() {

    val _searchTapStatus = MutableStateFlow<SearchTapStatus>(SearchTapStatus.Default)
    //val searchTapStatus = _searchTapStatus.asStateFlow()

    val _searchKeyWord = MutableStateFlow<String>("")

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


}