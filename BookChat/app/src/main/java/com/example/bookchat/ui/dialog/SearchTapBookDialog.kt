package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.DialogSearchTapBookClickedBinding
import com.example.bookchat.viewmodel.SearchTapBookDialogViewModel
import com.example.bookchat.viewmodel.ViewModelFactory

class SearchTapBookDialog(private val book: Book) : DialogFragment() {
    private lateinit var binding :DialogSearchTapBookClickedBinding
    private lateinit var searchTapBookDialogViewModel :SearchTapBookDialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_search_tap_book_clicked, container, false)
        searchTapBookDialogViewModel = ViewModelProvider(this, ViewModelFactory()).get(SearchTapBookDialogViewModel::class.java)
        binding.viewmodel = searchTapBookDialogViewModel
        searchTapBookDialogViewModel.book = book
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
    }

    //하트 눌렀다가 다시 누르면 어떻게 할 건가가
}