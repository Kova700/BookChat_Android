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
import com.example.bookchat.databinding.DialogCompleteBookTapClickedBinding
import com.example.bookchat.viewmodel.CompleteBookTapDialogViewModel
import com.example.bookchat.viewmodel.ViewModelFactory

class CompleteTapBookDialog(private val book: BookShelfItem) : DialogFragment() {
    private lateinit var binding : DialogCompleteBookTapClickedBinding
    private lateinit var completeBookTapDialogViewModel : CompleteBookTapDialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
        R.layout.dialog_complete_book_tap_clicked,container,false)
        completeBookTapDialogViewModel = ViewModelProvider(requireParentFragment(), ViewModelFactory()).get(
            CompleteBookTapDialogViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewmodel = completeBookTapDialogViewModel
        completeBookTapDialogViewModel.book = book
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}