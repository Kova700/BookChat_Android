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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookShelfViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<BookShelfEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val wishBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())
    val readingBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())
    val completeBookModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())

    var isWishBookLoaded = false
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
            isWishBookLoaded = true
            wishBookTotalCountCache = pair.second
            wishBookTotalCount.value = pair.second
            pair.first
        }
    }.cachedIn(viewModelScope)

    var isReadingBookLoaded = false
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
                isReadingBookLoaded = true
                readingBookTotalCountCache = pair.second
                readingBookTotalCount.value = pair.second
                pair.first
            }
        }.cachedIn(viewModelScope)
    }

    var isCompleteBookLoaded = false
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
                isCompleteBookLoaded = true
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

    fun deleteBookShelfBookWithSwipe(bookShelfItem: BookShelfItem) = viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(bookShelfItem.bookShelfId) }
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
                //지금 안에 들어가 있다면 지우는 방향으로 구현이 되어있음
                //이렇게 구현해버리면 같은 책의 페이지를 두번째 수정할 때 한 아이템의 EditEvent가 여러개가 쌓임
                // (뭐 딱히 상관은 없을듯 한데)
                // 중복된 EditEvent는 나중에 삭제해주도록 하자
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

    private fun applyEvents(
        paging: PagingData<BookShelfItem>,
        pagingViewEvent: PagingViewEvent,
    ): PagingData<BookShelfItem> {
        return when (pagingViewEvent) {
            is PagingViewEvent.Remove -> {
                paging.filter { it.bookShelfId != pagingViewEvent.bookShelfItem.bookShelfId }
            }
            is PagingViewEvent.RemoveWaiting -> {
                paging.filter { it.bookShelfId != pagingViewEvent.bookShelfItem.bookShelfId }
            }
            is PagingViewEvent.Edit -> {
                paging.map { bookshelfItem ->
                    if (pagingViewEvent.bookShelfItem.bookShelfId != bookshelfItem.bookShelfId) return@map bookshelfItem
                    return@map bookshelfItem.copy(pages = pagingViewEvent.bookShelfItem.pages)
                }
            }
        }
    }

    fun renewTotalItemCount(flag: String) {
        when (flag) {
            MODIFICATION_EVENT_FLAG_WISH -> {
                wishBookTotalCount.value =
                    wishBookTotalCountCache - getRemoveEventCount(wishBookModificationEvents)
            }
            MODIFICATION_EVENT_FLAG_READING -> {
                readingBookTotalCount.value =
                    readingBookTotalCountCache - getRemoveEventCount(readingBookModificationEvents)
            }
            MODIFICATION_EVENT_FLAG_COMPLETE -> {
                completeBookTotalCount.value =
                    completeBookTotalCountCache - getRemoveEventCount(completeBookModificationEvents)
            }
        }
    }

    private fun getRemoveEventCount(eventFlow :MutableStateFlow<List<PagingViewEvent>>) :Int{
        return eventFlow.value.count {
            (it is PagingViewEvent.Remove) || (it is PagingViewEvent.RemoveWaiting)
        }
    }

    fun startEvent(event: BookShelfEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class PagingViewEvent {
        data class Remove(val bookShelfItem: BookShelfItem) : PagingViewEvent()
        data class RemoveWaiting(val bookShelfItem: BookShelfItem) : PagingViewEvent()
        //이거 사용할 거면 Combined부분 다 손 좀 봐야함
        data class Edit(val bookShelfItem: BookShelfItem) : PagingViewEvent() //페이지 입력할 때 사용할 듯
        
    }

    sealed class BookShelfEvent {
        data class ChangeBookShelfTab(val tapIndex: Int) : BookShelfEvent()
    }

    companion object {
        const val MODIFICATION_EVENT_FLAG_WISH = "WISH"
        const val MODIFICATION_EVENT_FLAG_READING = "READING"
        const val MODIFICATION_EVENT_FLAG_COMPLETE = "COMPLETE"
    }
}