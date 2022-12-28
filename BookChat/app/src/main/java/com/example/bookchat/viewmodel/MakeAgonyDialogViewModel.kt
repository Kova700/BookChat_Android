package com.example.bookchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.repository.AgonyRepository
import com.example.bookchat.utils.AgonyFolderHexColor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow

class MakeAgonyDialogViewModel @AssistedInject constructor(
    private val agonyRepository : AgonyRepository,
    @Assisted val book: BookShelfItem
) : ViewModel() {

    val selectedColor = MutableStateFlow<AgonyFolderHexColor>(AgonyFolderHexColor.WHITE)
    
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

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: BookShelfItem) :MakeAgonyDialogViewModel
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