package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogBookReportExitBinding
import com.example.bookchat.viewmodel.BookReportViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookReportExitDialog : DialogFragment() {

    private lateinit var binding: DialogBookReportExitBinding
    private val bookReportViewModel: BookReportViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_book_report_exit, container, false)
        binding.dialog = this
        binding.lifecycleOwner = this
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    fun clickCancelBtn(){
        this.dismiss()
    }

    fun clickOkBtn(){
        this.dismiss()
        bookReportViewModel.bookReportStatus.value = BookReportViewModel.BookReportStatus.Loading
        bookReportViewModel.clickBackBtn()
    }
}