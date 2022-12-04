package com.example.bookchat.ui.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.DialogCompleteBookTapClickedBinding
import com.example.bookchat.ui.activity.AgonizeHistoryActivity
import com.example.bookchat.viewmodel.CompleteBookTapDialogViewModel
import com.example.bookchat.viewmodel.CompleteBookTapDialogViewModel.CompleteBookEvent
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

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

        observeEventFlow()

        //고민기록 연결
        //독후감 만들고 연결

        return binding.root
    }

    private fun observeEventFlow() {
        lifecycleScope.launch{
            completeBookTapDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    private fun handleEvent(event :CompleteBookEvent) = when(event){
        is CompleteBookEvent.OpenAgonize ->{ openAgonizeActivity() }
        is CompleteBookEvent.OpenBookReport ->{  }
    }

    private fun openAgonizeActivity(){
        val intent = Intent(requireContext(), AgonizeHistoryActivity::class.java)
            .putExtra(ReadingTapBookDialog.EXTRA_AGONIZE_BOOK,book)
        startActivity(intent)
    }

}