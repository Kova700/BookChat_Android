package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookReport
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.response.BookReportDoseNotExistException
import com.example.bookchat.repository.BookReportRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BookReportViewModel @AssistedInject constructor(
    private val bookReportRepository: BookReportRepository,
    @Assisted val book : BookShelfItem
) : ViewModel() {
    private val _eventFlow = MutableSharedFlow<BookReportUIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val bookReportStatus = MutableStateFlow<BookReportStatus>(BookReportStatus.Loading)

    val reportTitle = MutableStateFlow<String?>(null)
    val reportContent = MutableStateFlow<String?>(null)
    val reportCreatedAt = MutableStateFlow<String?>(null)

    var cachedTitle :String? = null
    var cachedContent :String? = null

    init {
        //Room사용해서 로컬에 데이터 있으면 가져오고
        //로컬에 데이터 없으면 호출해서 가져옴
        getBookReport()
            //넘겨받은 값이 없으면 입력창 노출하고
            //넘겨받은 값이 있으면 데이터 노출
    }

    //서버에서 가져옴
    //추후 UseCase 구분해서 로컬부터 우선적으로 가져오게 구현
    private fun getBookReport() = viewModelScope.launch{
        bookReportStatus.value = BookReportStatus.Loading
        runCatching { bookReportRepository.getBookReport(book) }
            .onSuccess { bookReport ->
                bindReport(bookReport)
                cacheReport(bookReport)
                bookReportStatus.value = BookReportStatus.ShowData
            }
            .onFailure { failHandler(it) }
    }

    private fun bindReport(bookReport :BookReport){
        reportTitle.value = bookReport.reportTitle
        reportContent.value = bookReport.reportContent
        reportCreatedAt.value = bookReport.reportCreatedAt
    }

    private fun cacheReport(bookReport :BookReport){
        cachedTitle = bookReport.reportTitle
        cachedContent = bookReport.reportContent
    }

    private fun registerBookReport(bookReport :BookReport) = viewModelScope.launch {
        bookReportStatus.value = BookReportStatus.Loading
        runCatching { bookReportRepository.registerBookReport(book, bookReport) }
            .onSuccess {
                cacheReport(bookReport)
                bookReportStatus.value = BookReportStatus.ShowData
            }
            .onFailure {
                bookReportStatus.value = BookReportStatus.InputData
                makeToast("독후감 등록이 실패했습니다.")
            }
    }

    fun deleteBookReport() = viewModelScope.launch {
        bookReportStatus.value = BookReportStatus.Loading
        runCatching { bookReportRepository.deleteBookReport(book) }
            .onSuccess {
                makeToast("독후감이 삭제되었습니다.")
                startEvent(BookReportUIEvent.MoveToBack)
            }
            .onFailure {
                bookReportStatus.value = BookReportStatus.ShowData
                makeToast("독후감 삭제를 실패했습니다.")
            }
    }

    private fun reviseBookReport(bookReport :BookReport) = viewModelScope.launch {
        bookReportStatus.value = BookReportStatus.Loading
        runCatching { bookReportRepository.reviseBookReport(book, bookReport) }
            .onSuccess {
                cacheReport(bookReport)
                bookReportStatus.value = BookReportStatus.ShowData
            }
            .onFailure {
                bookReportStatus.value = BookReportStatus.ReviseData
                makeToast("독후감 수정을 실패했습니다.")
            }
    }

    fun clickRegisterBtn(){
        if (isBookReportEmpty()) {
            makeToast("제목, 내용을 입력해주세요.")
            return
        }

        when(bookReportStatus.value){
            BookReportStatus.InputData -> {
                registerBookReport(BookReport(reportTitle.value!!, reportContent.value!!))
            }
            BookReportStatus.ReviseData -> {
                if (isNotChangedReport()) {
                    makeToast("수정된 내용이 없습니다.")
                    bookReportStatus.value = BookReportStatus.ShowData
                    return
                }
                reviseBookReport(BookReport(reportTitle.value!!, reportContent.value!!))
            }
            else -> { }
        }
    }

    fun isBookReportEmpty() :Boolean {
        return reportTitle.value.isNullOrEmpty() || reportContent.value.isNullOrEmpty()
    }

    fun isNotChangedReport() :Boolean{
        return (cachedTitle == reportTitle.value) && (cachedContent == reportContent.value)
    }

    fun clickReviseBtn(){
        bookReportStatus.value = BookReportStatus.ReviseData
    }

    fun clickDeleteBtn(){
        startEvent(BookReportUIEvent.ShowDeleteWarningDialog)
    }

    fun clickBackBtn(){
        startEvent(BookReportUIEvent.MoveToBack)
    }

    fun isEditingStatus() :Boolean{
        return (bookReportStatus.value == BookReportStatus.InputData) || (bookReportStatus.value == BookReportStatus.ReviseData)
    }

    fun initCachedData(){
        cachedTitle = null
        cachedContent = null
    }

    private fun startEvent (event : BookReportUIEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext,text,Toast.LENGTH_SHORT).show()
    }

    private fun failHandler(exception: Throwable) {
        when (exception) {
            is BookReportDoseNotExistException -> bookReportStatus.value = BookReportStatus.InputData
            else -> startEvent(BookReportUIEvent.UnknownError)
        }
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem) :BookReportViewModel
    }

    sealed class BookReportStatus{
        object Loading :BookReportStatus()
        object ShowData :BookReportStatus() //데이터 노출
        object InputData :BookReportStatus() //입력창 노출 (호출 API 구분을 위해 Input , Revise 구분)
        object ReviseData :BookReportStatus() //입력창 노출 (호출 API 구분을 위해 Input , Revise 구분)
    }

    sealed class BookReportUIEvent{
        object MoveToBack :BookReportUIEvent()
        object UnknownError :BookReportUIEvent()
        object ShowDeleteWarningDialog :BookReportUIEvent()
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            book: BookShelfItem
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(book) as T
            }
        }
    }

}