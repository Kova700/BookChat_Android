package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookReport
import com.example.bookchat.data.BookShelfItem
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

    var cachedTitle = ""
    var cachedContent = ""

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
        runCatching { bookReportRepository.getBookReport(book) }
            .onSuccess { bookReport ->
                bindReport(bookReport)
                cacheReport(bookReport)
                bookReportStatus.value = BookReportStatus.ShowData
            }
            .onFailure {
                //수정이 필요함 실패가 아닌 404일 때만 InputData로 바뀌게
                bookReportStatus.value = BookReportStatus.InputData
            }
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
        runCatching { bookReportRepository.registerBookReport(book, bookReport) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"독후감이 등록되었습니다.",Toast.LENGTH_SHORT).show()
                cacheReport(bookReport)
                bookReportStatus.value = BookReportStatus.ShowData
            }
            .onFailure {
                bookReportStatus.value = BookReportStatus.InputData
                Toast.makeText(App.instance.applicationContext,"독후감 등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteBookReport() = viewModelScope.launch {
        runCatching { bookReportRepository.deleteBookReport(book) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"독후감이 삭제되었습니다.",Toast.LENGTH_SHORT).show()
                startEvent(BookReportUIEvent.MoveToBack)
            }
            .onFailure {
                bookReportStatus.value = BookReportStatus.ShowData
                Toast.makeText(App.instance.applicationContext,"독후감 삭제 실패",Toast.LENGTH_SHORT).show()
            }
    }

    private fun reviseBookReport(bookReport :BookReport) = viewModelScope.launch {
        runCatching { bookReportRepository.reviseBookReport(book, bookReport) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"독후감이 수정되었습니다.",Toast.LENGTH_SHORT).show()
                cacheReport(bookReport)
                bookReportStatus.value = BookReportStatus.ShowData
            }
            .onFailure {
                bookReportStatus.value = BookReportStatus.ReviseData
                Toast.makeText(App.instance.applicationContext,"독후감 수정 실패",Toast.LENGTH_SHORT).show()
            }
    }

    fun clickRegisterBtn(){
        if (isBookReportEmpty()) {
            Toast.makeText(App.instance.applicationContext,"제목, 내용을 입력해주세요.",Toast.LENGTH_SHORT).show()
            return
        }

        when(bookReportStatus.value){
            BookReportStatus.InputData -> {
                bookReportStatus.value = BookReportStatus.Loading
                registerBookReport(BookReport(reportTitle.value!!, reportContent.value!!))
            }
            BookReportStatus.ReviseData -> {
                if (isNotChangedReport()) {
                    Toast.makeText(App.instance.applicationContext,"수정된 내용이 없습니다.",Toast.LENGTH_SHORT).show()
                    bookReportStatus.value = BookReportStatus.ShowData
                    return
                }
                bookReportStatus.value = BookReportStatus.Loading
                reviseBookReport(BookReport(reportTitle.value!!, reportContent.value!!))
            }
            else -> { }
        }
    }

    private fun isBookReportEmpty() :Boolean {
        return reportTitle.value.isNullOrEmpty() || reportContent.value.isNullOrEmpty()
    }

    private fun isNotChangedReport() :Boolean{
        return (cachedTitle == reportTitle.value) && (cachedContent == reportContent.value)
    }

    fun clickReviseBtn(){
        bookReportStatus.value = BookReportStatus.ReviseData
    }

    fun clickDeleteBtn(){
        //삭제시 다이얼로그로 경고 문구 띄워야함
        bookReportStatus.value = BookReportStatus.Loading
        deleteBookReport()
    }

    fun clickBackBtn(){
        //작성 중이거나 , 작성중인 내용이 있다면 뒤로가기시에 다이얼로그로 경고 문구 띄워야함
        initCachedData()
        startEvent(BookReportUIEvent.MoveToBack)
    }

    fun initCachedData(){
        cachedTitle = ""
        cachedContent = ""
    }

    private fun startEvent (event : BookReportUIEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
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