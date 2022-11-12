package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository

class WishBookTapBookDialogViewModel(private val bookRepository: BookRepository) :ViewModel() {
    lateinit var book: BookShelfItem
}