package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.SearchTapStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel(private var bookRepository :BookRepository) :ViewModel() {

    private val _searchTapStatus = MutableStateFlow<SearchTapStatus>(SearchTapStatus.Default)
    val searchTapStatus = _searchTapStatus.asStateFlow()

}