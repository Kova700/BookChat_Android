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
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogBookReportExitBinding
import com.example.bookchat.viewmodel.BookReportViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookReportWarningDialog : DialogFragment() {

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
        setWarningText()

        return binding.root
    }

    private fun setWarningText(){
        if (bookReportViewModel.isEditingStatus()){
            binding.warningTextView.setText(R.string.message_warning_cancel_write_book_report)
            return
        }
        binding.warningTextView.setText(R.string.message_warning_delete_book_report)
    }

    fun clickCancelBtn(){
        this.dismiss()
    }

    fun clickOkBtn(){
        if (bookReportViewModel.isEditingStatus()){
            this.dismiss()
            bookReportViewModel.bookReportStatus.value = BookReportViewModel.BookReportStatus.Loading
            bookReportViewModel.clickBackBtn()
            return
        }
        bookReportViewModel.deleteBookReport()
    }
}