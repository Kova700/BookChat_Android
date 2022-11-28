package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.paging.ReadingBookTapPagingSource
import com.example.bookchat.paging.WishBookTapPagingSource
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.BookRepository.Companion.READING_TAP_BOOKS_ITEM_LOAD_SIZE
import com.example.bookchat.repository.BookRepository.Companion.WISH_TAP_BOOKS_ITEM_LOAD_SIZE
import com.example.bookchat.utils.ReadingStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookShelfViewModel(private val bookRepository: BookRepository) : ViewModel() {
    private val readingBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())
    private val wishBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())

    var wishBookTotalCount = MutableStateFlow<Long>(0)
    private val wishBookResult by lazy {
        Pager(
            config = PagingConfig(
                pageSize = WISH_TAP_BOOKS_ITEM_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { WishBookTapPagingSource() }
        ).flow.map {
            it.map {
                wishBookTotalCount.value = it.second
                it.first
            }
        }.cachedIn(viewModelScope)
    }

    var readingBookTotalCount = MutableStateFlow<Long>(0)
    private val readingBookResult by lazy {
        Pager(
            config = PagingConfig(
                pageSize = READING_TAP_BOOKS_ITEM_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ReadingBookTapPagingSource() }
        ).flow.map {
            it.map {
                readingBookTotalCount.value = it.second
                it.first
            }
        }.cachedIn(viewModelScope)
    }

    val readingBookCombined by lazy {
        readingBookResult.combine(readingBookModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    val wishBookCombined by lazy {
        wishBookResult.combine(wishBookModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    fun deleteBookShelfBook(book: BookShelfItem) = viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(book.bookId) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext, "Reading 삭제 성공", Toast.LENGTH_SHORT)
                    .show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext, "Reading 삭제 실패", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun onPagingViewEvent(pagingViewEvent: PagingViewEvent, readingStatus :ReadingStatus) {
        when(readingStatus){
            ReadingStatus.WISH -> { wishBookModificationEvents.value += pagingViewEvent }
            ReadingStatus.READING ->{ readingBookModificationEvents.value += pagingViewEvent }
            ReadingStatus.COMPLETE -> { }
        }
    }

    private fun applyEvents(
        paging: PagingData<BookShelfItem>,
        pagingViewEvent: PagingViewEvent
    ): PagingData<BookShelfItem> {
        return when (pagingViewEvent) {
            is PagingViewEvent.Remove -> {
                var itemCount = 0L
                val newPagingData =
                    paging.filter { it.bookId != pagingViewEvent.bookShelfItem.bookId }
                newPagingData.map { readingBookTotalCount.value = ++itemCount;it }
            }
        }
    }

    sealed class PagingViewEvent {
        data class Remove(val bookShelfItem: BookShelfItem) : PagingViewEvent()
//        data class Edit(val bookShelfItem: BookShelfItem) : PagingViewEvent() //페이지 입력할 때 사용할 듯
    }

}