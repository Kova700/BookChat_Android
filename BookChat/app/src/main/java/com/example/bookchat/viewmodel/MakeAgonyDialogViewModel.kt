package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.AgonyRepository
import com.example.bookchat.data.request.RequestMakeAgony
import com.example.bookchat.utils.AgonyFolderHexColor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MakeAgonyDialogViewModel @AssistedInject constructor(
    private val agonyRepository : AgonyRepository,
    @Assisted val book: BookShelfItem
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<MakeAgonyUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val selectedColor = MutableStateFlow<AgonyFolderHexColor>(AgonyFolderHexColor.WHITE)
    val agonyTitle = MutableStateFlow<String?>(null)
    
    //글자 최대 길이 설정 + 글자 깨짐 + 간격 설정남음

    fun clickColorCircle(color :AgonyFolderHexColor){
        when(color){
            AgonyFolderHexColor.WHITE -> { selectedColor.value = AgonyFolderHexColor.WHITE }
            AgonyFolderHexColor.BLACK -> { selectedColor.value = AgonyFolderHexColor.BLACK }
            AgonyFolderHexColor.PURPLE -> { selectedColor.value = AgonyFolderHexColor.PURPLE }
            AgonyFolderHexColor.MINT -> { selectedColor.value = AgonyFolderHexColor.MINT }
            AgonyFolderHexColor.GREEN -> { selectedColor.value = AgonyFolderHexColor.GREEN }
            AgonyFolderHexColor.YELLOW -> { selectedColor.value = AgonyFolderHexColor.YELLOW }
            AgonyFolderHexColor.ORANGE -> { selectedColor.value = AgonyFolderHexColor.ORANGE }
        }
    }

    fun clickRegisterBtn(){
        if (agonyTitle.value.isNullOrBlank()){
            makeToast("주제를 입력해 주세요.")
            return
        }
        registeAgony(RequestMakeAgony(agonyTitle.value!!.trim(), selectedColor.value))
    }

    private fun registeAgony(requestMakeAgony : RequestMakeAgony) = viewModelScope.launch{
        runCatching { agonyRepository.makeAgony(book, requestMakeAgony) }
            .onSuccess { startEvent(MakeAgonyUiEvent.RenewAgonyList) }
            .onFailure { makeToast("고민 등록을 실패했습니다.") }
    }

    private fun startEvent (event : MakeAgonyUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class MakeAgonyUiEvent{
        object RenewAgonyList :MakeAgonyUiEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem) :MakeAgonyDialogViewModel
    }

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
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