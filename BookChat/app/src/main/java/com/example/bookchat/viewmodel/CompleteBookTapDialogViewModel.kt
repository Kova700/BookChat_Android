package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow

class CompleteBookTapDialogViewModel(private val bookRepository: BookRepository) : ViewModel() {
    var starRating = MutableStateFlow<Float>(0.0F)
    lateinit var book: BookShelfItem
}