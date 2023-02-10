package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.databinding.DialogCompleteBookSetStarsBinding
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.toStarRating
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.ReadingBookTapDialogViewModel
import com.example.bookchat.viewmodel.SearchTapBookDialogViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CompleteBookSetStarsDialog @AssistedInject constructor(
    private val bookRepository: BookRepository,
    @Assisted val book :Book
) : DialogFragment() {

    private lateinit var binding :DialogCompleteBookSetStarsBinding

    val starRating = MutableStateFlow<Float>(0.0F)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_complete_book_set_stars, container, false)
        with(binding){
            lifecycleOwner = this@CompleteBookSetStarsDialog
            dialog = this@CompleteBookSetStarsDialog
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    fun clickOkBtn(){
        if (starRating.value == 0F){
            makeToast("별점을 등록해주세요.")
            return
        }
        requestRegisterCompleteBook()
    }

    fun clickCancelBtn(){
        this.dismiss()
    }

    fun requestRegisterCompleteBook() = lifecycleScope.launch {
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.COMPLETE, starRating.value.toStarRating())
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                makeToast("[서재] - [독서완료]에 도서가 등록되었습니다.")
                this@CompleteBookSetStarsDialog.dismiss()
            }
            .onFailure { makeToast("독서완료 등록에 실패했습니다.") }
    }

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(book: Book) : CompleteBookSetStarsDialog
    }

}