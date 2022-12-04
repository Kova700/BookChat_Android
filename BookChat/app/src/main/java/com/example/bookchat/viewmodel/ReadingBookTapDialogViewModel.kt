package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.StarRating
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ReadingBookTapDialogViewModel(private val bookRepository: BookRepository) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<ReadingBookEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    lateinit var book: BookShelfItem
    var starRating = MutableStateFlow<Float>(0.0F)

    fun changeToCompleteBook() = viewModelScope.launch{
        //book.setStarRating(starRating.value) //별점도 같이 기입 가능하게 API 수정 대기 중
        Log.d(TAG, "ReadingBookTapDialogViewModel: changeToCompleteBook() - book.starRating : ${book.star}")
        runCatching { bookRepository.changeBookShelfBookStatus(book,ReadingStatus.COMPLETE) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"독서완료로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                startEvent(ReadingBookEvent.MoveToCompleteBook)
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"독서완료로 변경 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startEvent (event : ReadingBookEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class ReadingBookEvent {
        object MoveToCompleteBook : ReadingBookEvent()
    }
}