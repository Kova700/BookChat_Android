package com.example.bookchat.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.DialogPageInputBottomSheetBinding
import com.example.bookchat.ui.fragment.BookShelfFragment
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.PageInputDialogViewModel
import com.example.bookchat.viewmodel.PageInputDialogViewModel.PageInputDialogEvnet
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PageInputBottomSheetDialog(private val bookShelfDataItem: BookShelfDataItem) :
    BottomSheetDialogFragment() {
    @Inject
    lateinit var pageInputDialogViewModelFactory: PageInputDialogViewModel.AssistedFactory

    private lateinit var binding: DialogPageInputBottomSheetBinding
    val pageInputDialogViewModel: PageInputDialogViewModel by viewModels {
        PageInputDialogViewModel.provideFactory(pageInputDialogViewModelFactory, bookShelfDataItem)
    }
    val bookShelfViewModel: BookShelfViewModel by viewModels({ getBookShelfFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_page_input_bottom_sheet, container, false)
        binding.viewmodel = pageInputDialogViewModel
        binding.lifecycleOwner = this

        observeEvent()

        return binding.root
    }

    private fun getBookShelfFragment(): BookShelfFragment {
        var fragment = requireParentFragment()
        while (fragment !is BookShelfFragment) {
            fragment = fragment.requireParentFragment()
        }
        return fragment
    }

    private fun observeEvent() = lifecycleScope.launch {
        pageInputDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    private fun handleEvent(event: PageInputDialogEvnet) = when (event) {
        is PageInputDialogEvnet.CloseDialog -> {
            this.dismiss()
        }
        is PageInputDialogEvnet.SuccessApi -> {
            val editEvent = BookShelfViewModel.PagingViewEvent.Edit(event.bookShelfDataItem)
            bookShelfViewModel.addPagingViewEvent(editEvent, ReadingStatus.READING)
            this.dismiss()
        }
    }

}