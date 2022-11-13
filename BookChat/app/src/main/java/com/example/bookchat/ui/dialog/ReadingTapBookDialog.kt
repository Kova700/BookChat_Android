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
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.DialogReadingBookTapClickedBinding
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.ReadingBookTapDialogViewModel
import com.example.bookchat.viewmodel.ViewModelFactory

class ReadingTapBookDialog(private val book: BookShelfItem) : DialogFragment() {
    private lateinit var binding : DialogReadingBookTapClickedBinding
    private lateinit var readingBookTapDialogViewModel : ReadingBookTapDialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.dialog_reading_book_tap_clicked,container,false)
        readingBookTapDialogViewModel = ViewModelProvider(requireParentFragment(), ViewModelFactory()).get(
            ReadingBookTapDialogViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewmodel = readingBookTapDialogViewModel
        readingBookTapDialogViewModel.book = book
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        readingBookTapDialogViewModel.starRating.value = 0F
        super.onDestroyView()
    }
}