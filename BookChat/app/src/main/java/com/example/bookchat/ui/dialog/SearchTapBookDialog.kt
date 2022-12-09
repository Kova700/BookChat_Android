package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.DialogSearchTapBookClickedBinding
import com.example.bookchat.viewmodel.SearchTapBookDialogViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchTapBookDialog(private val book: Book) : DialogFragment() {

    @Inject
    lateinit var searchTapBookDialogViewModelFactory: SearchTapBookDialogViewModel.AssistedFactory

    private lateinit var binding :DialogSearchTapBookClickedBinding
    private val searchTapBookDialogViewModel :SearchTapBookDialogViewModel by viewModels {
        SearchTapBookDialogViewModel.provideFactory(searchTapBookDialogViewModelFactory,book)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_search_tap_book_clicked, container, false)
        binding.viewmodel = searchTapBookDialogViewModel
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //하트 눌렀다가 다시 누르면 어떻게 할 건가가
}