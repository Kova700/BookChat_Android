package com.example.bookchat.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.repository.AgonyRepository
import com.example.bookchat.utils.AgonyFolderHexColor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MakeAgonyDialogViewModel @AssistedInject constructor(
	private val agonyRepository: AgonyRepository,
	@Assisted val book: BookShelfItem
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<MakeAgonyUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val selectedColor = MutableStateFlow<AgonyFolderHexColor>(AgonyFolderHexColor.WHITE)
    val agonyTitle = MutableStateFlow<String>("")

    //글자 최대 길이 설정 + 글자 깨짐 + 간격 설정남음
    fun clickColorCircle(color: AgonyFolderHexColor) {
        selectedColor.value = color
    }

    fun clickRegisterBtn() {
        if (agonyTitle.value.trim().isBlank()) {
            makeToast(R.string.agony_make_empty)
            return
        }
        registerAgony(agonyTitle.value.trim(), selectedColor.value)
    }

    private fun registerAgony(
        title: String,
        hexColorCode: AgonyFolderHexColor
    ) = viewModelScope.launch {
        runCatching { agonyRepository.makeAgony(book.bookShelfId, title, hexColorCode) }
            .onSuccess { startEvent(MakeAgonyUiEvent.RenewAgonyList) }
            .onFailure { makeToast(R.string.agony_make_fail) }
    }

    private fun startEvent(event: MakeAgonyUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class MakeAgonyUiEvent {
        object RenewAgonyList : MakeAgonyUiEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem): MakeAgonyDialogViewModel
    }

    private fun makeToast(stringId: Int) {
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
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