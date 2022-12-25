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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.DialogCompleteBookTapClickedBinding
import com.example.bookchat.ui.activity.AgonyActivity
import com.example.bookchat.ui.activity.BookReportActivity
import com.example.bookchat.viewmodel.CompleteBookTapDialogViewModel
import com.example.bookchat.viewmodel.CompleteBookTapDialogViewModel.CompleteBookEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CompleteTapBookDialog(private val book: BookShelfItem) : DialogFragment() {

    @Inject
    lateinit var completeBookTapDialogViewModelFactory : CompleteBookTapDialogViewModel.AssistedFactory

    private lateinit var binding : DialogCompleteBookTapClickedBinding
    private val completeBookTapDialogViewModel : CompleteBookTapDialogViewModel by viewModels {
        CompleteBookTapDialogViewModel.provideFactory(completeBookTapDialogViewModelFactory, book)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
        R.layout.dialog_complete_book_tap_clicked,container,false)
        binding.lifecycleOwner = this
        binding.viewmodel = completeBookTapDialogViewModel
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
        is CompleteBookEvent.OpenBookReport ->{ openBookReportActivity() }
    }

    private fun openAgonizeActivity(){
        val intent = Intent(requireContext(), AgonyActivity::class.java)
            .putExtra(ReadingTapBookDialog.EXTRA_AGONIZE_BOOK,book)
        startActivity(intent)
    }

    private fun openBookReportActivity(){
        val intent = Intent(requireContext(), BookReportActivity::class.java)
            .putExtra(EXTRA_BOOKREPORT_BOOK,book)
        startActivity(intent)
    }

    companion object{
        const val EXTRA_BOOKREPORT_BOOK = "EXTRA_BOOKREPORT_BOOK"
    }

}