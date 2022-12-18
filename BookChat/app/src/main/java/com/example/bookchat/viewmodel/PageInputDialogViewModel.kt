package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.ReadingStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PageInputDialogViewModel @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val book: BookShelfItem
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<PageInputDialogEvnet>()
    val eventFlow = _eventFlow.asSharedFlow()

    val inputedPage = MutableStateFlow<String>(book.pages.toString()) //그전에 얘로 숫자 핸들링
    val pageInputedBook = MutableStateFlow<BookShelfItem>(book.copy()) //마지막에 API 성공시에 값 바꾸기

    //페이지가 변경된 실시간 갱신되는 BookShelfItem가 필요함

    fun clickFinishBtn() {
        if(book.pages.toString() == inputedPage.value) {
            startEvent(PageInputDialogEvnet.CloseDialog)
            return
        }

        pageInputedBook.value.pages = inputedPage.value.toInt()
        registerReadingPage()
    }

    fun inputNumber(num: Int) {
        if (inputedPage.value == "0") inputedPage.value = ""
        inputedPage.value += num.toString()
    }

    fun deleteNumber() {
        if (inputedPage.value == "0") return

        if (inputedPage.value.length == 1) {
            inputedPage.value = "0"
            return
        }

        inputedPage.value = inputedPage.value.substring(0, inputedPage.value.lastIndex)
    }

    private fun registerReadingPage() = viewModelScope.launch {
        runCatching { bookRepository.changeBookShelfBookStatus(pageInputedBook.value, ReadingStatus.READING)}
            .onSuccess {
                Toast.makeText(App.instance.applicationContext, "페이지 등록 성공", Toast.LENGTH_SHORT).show()
                startEvent(PageInputDialogEvnet.SuccessApi(pageInputedBook.value))
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext, "페이지 등록 실패", Toast.LENGTH_SHORT).show()
            }
    }

    fun startEvent(event: PageInputDialogEvnet) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class PageInputDialogEvnet {
        object CloseDialog : PageInputDialogEvnet()
        data class SuccessApi(val book: BookShelfItem) : PageInputDialogEvnet()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem): PageInputDialogViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            book: BookShelfItem
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(book) as T
            }
        }
    }

}