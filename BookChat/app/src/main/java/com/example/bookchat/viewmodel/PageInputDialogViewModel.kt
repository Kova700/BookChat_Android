package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfDataItem
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
    @Assisted val bookShelfDataItem: BookShelfDataItem
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<PageInputDialogEvnet>()
    val eventFlow = _eventFlow.asSharedFlow()

    val inputedPage = MutableStateFlow<String>(bookShelfDataItem.bookShelfItem.pages.toString()) //입력 숫자 핸들링

    fun clickFinishBtn() {
        if(bookShelfDataItem.bookShelfItem.pages.toString() == inputedPage.value) {
            startEvent(PageInputDialogEvnet.CloseDialog)
            return
        }

        registerReadingPage(bookShelfDataItem.copy(
            bookShelfItem = bookShelfDataItem.bookShelfItem.copy(
                pages = inputedPage.value.toInt()
            )
        ))
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

    private fun registerReadingPage(newBookShelfDataItem :BookShelfDataItem) = viewModelScope.launch {
        runCatching { bookRepository.changeBookShelfBookStatus(newBookShelfDataItem.bookShelfItem, ReadingStatus.READING)}
            .onSuccess { startEvent(PageInputDialogEvnet.SuccessApi(newBookShelfDataItem)) }
            .onFailure { makeToast("페이지 등록을 실패했습니다.") }
    }

    fun startEvent(event: PageInputDialogEvnet) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    sealed class PageInputDialogEvnet {
        object CloseDialog : PageInputDialogEvnet()
        data class SuccessApi(val bookShelfDataItem: BookShelfDataItem) : PageInputDialogEvnet()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(bookShelfDataItem: BookShelfDataItem): PageInputDialogViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            bookShelfDataItem: BookShelfDataItem
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(bookShelfDataItem) as T
            }
        }
    }

}