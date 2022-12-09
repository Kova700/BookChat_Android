package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.RequestRegisterBookShelfBook
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SearchTapBookDialogViewModel(private val bookRepository: BookRepository) : ViewModel() {
    lateinit var book: Book
    val isAlreadyInBookShelf = MutableStateFlow(false)

    init {
        //checkAlreadyInBookShelf를 호출하고 싶은데
        //book이 존재하지 않을때라서 문제가 되는 상황
        //book을 ViewModelFactory로 파라미터로 넘겨주기엔 코드가 너무 더러워 지는 상황
        //의존성 주입 가즈아!~
   }

    fun checkAlreadyInBookShelf() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: checkAlreadyInBookShelf() - called")
        runCatching { bookRepository.checkAlreadyInBookShelf(book) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"서재에 존재여부 확인 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"서재에 존재여부 확인 실패",Toast.LENGTH_SHORT).show()
            }
    }

    fun requestRegisterWishBook() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: requestRegisterWishBook() - called")
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Wish등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Wish등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

    fun requestRegisterReadingBook() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: requestRegisterReadingBook() - called")
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book,ReadingStatus.READING)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Reading등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Reading등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

}