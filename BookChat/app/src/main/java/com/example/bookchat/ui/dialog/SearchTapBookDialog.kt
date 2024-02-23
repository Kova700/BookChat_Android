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
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.DialogSearchTapBookClickedBinding
import com.example.bookchat.ui.viewmodel.SearchTapBookDialogViewModel
import com.example.bookchat.ui.viewmodel.SearchTapBookDialogViewModel.SearchTapDialogEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchTapBookDialog(private val book: Book) : DialogFragment() {

    @Inject
    lateinit var searchTapBookDialogViewModelFactory: SearchTapBookDialogViewModel.AssistedFactory
    @Inject
    lateinit var completeBookSetStarsDialogFactory : CompleteBookSetStarsDialog.AssistedFactory

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
        with(binding){
            lifecycleOwner = this@SearchTapBookDialog
            viewmodel = searchTapBookDialogViewModel
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        observeEvent()

        return binding.root
    }

    private fun observeEvent(){
        lifecycleScope.launch {
            searchTapBookDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    private fun openSetStarsDialog(){
        val dialog = completeBookSetStarsDialogFactory.create(book)
        dialog.show(childFragmentManager, DIALOG_TAG_STAR_SET)
    }

    private fun handleEvent(event: SearchTapDialogEvent) = when(event){
        is SearchTapDialogEvent.OpenSetStarsDialog -> openSetStarsDialog()
    }

    companion object{
        const val DIALOG_TAG_STAR_SET = "DIALOG_TAG_STAR_SET"
    }

}