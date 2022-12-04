package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.paging.CompleteBookTapPagingSource
import com.example.bookchat.paging.ReadingBookTapPagingSource
import com.example.bookchat.paging.WishBookTapPagingSource
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.repository.BookRepository.Companion.COMPLETE_TAP_BOOKS_ITEM_LOAD_SIZE
import com.example.bookchat.repository.BookRepository.Companion.READING_TAP_BOOKS_ITEM_LOAD_SIZE
import com.example.bookchat.repository.BookRepository.Companion.WISH_TAP_BOOKS_ITEM_LOAD_SIZE
import com.example.bookchat.utils.ReadingStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookShelfViewModel(private val bookRepository: BookRepository) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<BookShelfEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val wishBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())
    val readingBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())
    val completeBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())

    var wishBookTotalCountCache = 0L
    var wishBookTotalCount = MutableStateFlow<Long>(0)
    var wishBookResult = Pager(
        config = PagingConfig(
            pageSize = WISH_TAP_BOOKS_ITEM_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { WishBookTapPagingSource() }
    ).flow.map { pagingData ->
        pagingData.map { pair ->
            wishBookTotalCountCache = pair.second
            wishBookTotalCount.value = pair.second
            pair.first
        }
    }.cachedIn(viewModelScope)

    var readingBookTotalCountCache = 0L
    var readingBookTotalCount = MutableStateFlow<Long>(0)
    private val readingBookResult by lazy {
        Pager(
            config = PagingConfig(
                pageSize = READING_TAP_BOOKS_ITEM_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ReadingBookTapPagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { pair ->
                readingBookTotalCountCache = pair.second
                readingBookTotalCount.value = pair.second
                pair.first
            }
        }.cachedIn(viewModelScope)
    }

    var completeBookTotalCountCache = 0L
    var completeBookTotalCount = MutableStateFlow<Long>(0)
    private val completeBookResult by lazy {
        Pager(
            config = PagingConfig(
                pageSize = COMPLETE_TAP_BOOKS_ITEM_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CompleteBookTapPagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { pair ->
                completeBookTotalCountCache = pair.second
                completeBookTotalCount.value = pair.second
                pair.first
            }
        }.cachedIn(viewModelScope)
    }

    val wishBookCombined by lazy {
        wishBookResult.combine(wishBookModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
                .also { renewTotalItemCount(MODIFICATION_EVENT_FLAG_WISH) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    val readingBookCombined by lazy {
        readingBookResult.combine(readingBookModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
                .also { renewTotalItemCount(MODIFICATION_EVENT_FLAG_READING) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    val completeBookCombined by lazy {
        completeBookResult.combine(completeBookModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
                .also { renewTotalItemCount(MODIFICATION_EVENT_FLAG_COMPLETE) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    fun deleteBookShelfBookWithSwipe(book: BookShelfItem) = viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(book.bookId) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext, "도서가 삭제되었습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext, "도서 삭제 실패", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun onPagingViewEvent(pagingViewEvent: PagingViewEvent, readingStatus: ReadingStatus) {
        when (readingStatus) {
            ReadingStatus.WISH -> {
                wishBookModificationEvents.value += pagingViewEvent
            }
            ReadingStatus.READING -> {
                if (readingBookModificationEvents.value.contains(pagingViewEvent)){
                    readingBookModificationEvents.value -= pagingViewEvent
                    return
                }
                readingBookModificationEvents.value += pagingViewEvent
            }
            ReadingStatus.COMPLETE -> {
                if (completeBookModificationEvents.value.contains(pagingViewEvent)){
                    completeBookModificationEvents.value -= pagingViewEvent
                    return
                }
                completeBookModificationEvents.value += pagingViewEvent
            }
        }
    }

    fun applyEvents(
        paging: PagingData<BookShelfItem>,
        pagingViewEvent: PagingViewEvent,
    ): PagingData<BookShelfItem> {
        return when (pagingViewEvent) {
            is PagingViewEvent.Remove -> {
                paging.filter { it.bookId != pagingViewEvent.bookShelfItem.bookId }
            }
//            is PagingViewEvent.Edit -> {  }
        }
    }

    private fun renewTotalItemCount(flag: String) {
        when (flag) {
            MODIFICATION_EVENT_FLAG_WISH -> {
                wishBookTotalCount.value =
                    wishBookTotalCountCache - wishBookModificationEvents.value.size
            }
            MODIFICATION_EVENT_FLAG_READING -> {
                readingBookTotalCount.value =
                    readingBookTotalCountCache - readingBookModificationEvents.value.size
            }
            MODIFICATION_EVENT_FLAG_COMPLETE -> {
                completeBookTotalCount.value =
                    completeBookTotalCountCache - completeBookModificationEvents.value.size
            }
        }
    }

    fun startEvent(event: BookShelfEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class PagingViewEvent {
        data class Remove(val bookShelfItem: BookShelfItem) : PagingViewEvent()
//        data class Edit(val bookShelfItem: BookShelfItem) : PagingViewEvent() //페이지 입력할 때 사용할 듯
    }

    sealed class BookShelfEvent {
        data class ChangeBookShelfTab(val tapIndex: Int) : BookShelfEvent()
    }

    companion object {
        private const val MODIFICATION_EVENT_FLAG_WISH = "WISH"
        private const val MODIFICATION_EVENT_FLAG_READING = "READING"
        private const val MODIFICATION_EVENT_FLAG_COMPLETE = "COMPLETE"
    }
}