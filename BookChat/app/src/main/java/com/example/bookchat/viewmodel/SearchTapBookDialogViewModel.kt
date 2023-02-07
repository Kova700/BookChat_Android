package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.utils.ReadingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SearchTapBookDialogViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val book :Book
) : ViewModel()
{
    val isAlreadyInBookShelf = MutableStateFlow<RespondCheckInBookShelf?>(null)
    var isToggleChecked = MutableStateFlow<Boolean>(false)

    init {
        checkAlreadyInBookShelf()
   }

    fun requestToggleApi() = viewModelScope.launch {
        isToggleChecked.value = !(isToggleChecked.value)

        if(isToggleChecked.value){
            requestRegisterWishBook()
            return@launch
        }
        isAlreadyInBookShelf.value?.let { requestRemoveWishBook(it) }
    }

    fun clickCompleteBtn(){
        //레이팅바 노출
        //버튼 비활성화
        //별점 채워지면 다시 버튼 활성화
        //활성화 된 버튼 다시 누르면 독서완료 요청 API
    }

    private fun checkAlreadyInBookShelf() = viewModelScope.launch {
        runCatching { bookRepository.checkAlreadyInBookShelf(book) }
            .onSuccess { respondCheckInBookShelf ->
                respondCheckInBookShelf?.let {
                    isAlreadyInBookShelf.value = respondCheckInBookShelf
                    isToggleChecked.value = true
                }
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"서재에 존재여부 확인 실패",Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestRegisterWishBook() = viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Wish등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Wish등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun requestRemoveWishBook(respondCheckInBookShelf : RespondCheckInBookShelf)= viewModelScope.launch {
        runCatching { bookRepository.deleteBookShelfBook(respondCheckInBookShelf.bookId) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext, "도서가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext, "WISH 삭제 실패", Toast.LENGTH_SHORT).show()
            }
    }

    fun requestRegisterReadingBook() = viewModelScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book,ReadingStatus.READING)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Reading등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Reading등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: Book) :SearchTapBookDialogViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            book: Book
        ) :ViewModelProvider.Factory = object :ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(book) as T
            }
        }
    }

}