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
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.DialogWishBookTapClickedBinding
import com.example.bookchat.ui.fragment.BookShelfFragment
import com.example.bookchat.ui.fragment.ReadingBookShelfFragment
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.ui.viewmodel.BookShelfViewModel
import com.example.bookchat.ui.viewmodel.BookShelfViewModel.BookShelfEvent
import com.example.bookchat.ui.viewmodel.BookShelfViewModel.Companion.READING_TAB_INDEX
import com.example.bookchat.ui.viewmodel.WishBookTapDialogViewModel
import com.example.bookchat.ui.viewmodel.WishBookTapDialogViewModel.WishBookEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WishTapBookDialog(private val bookShelfDataItem: BookShelfDataItem) : DialogFragment() {

    @Inject
    lateinit var wishBookTapDialogViewModelFactory :WishBookTapDialogViewModel.AssistedFactory

    private lateinit var binding :DialogWishBookTapClickedBinding
    private val wishBookTapDialogViewModel: WishBookTapDialogViewModel by viewModels{
        WishBookTapDialogViewModel.provideFactory(wishBookTapDialogViewModelFactory, bookShelfDataItem)
    }
    private val bookShelfViewModel: BookShelfViewModel by viewModels({ getBookShelfFragment() })
    private var returnEvent :WishBookEvent? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_wish_book_tap_clicked, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = wishBookTapDialogViewModel
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        observeEventFlow()

        return binding.root
    }

    private fun getBookShelfFragment() : BookShelfFragment {
        var fragment = requireParentFragment()
        while (fragment !is BookShelfFragment){
            fragment = fragment.requireParentFragment()
        }
        return fragment
    }

    private fun getReadingBookTabFragment() : ReadingBookShelfFragment {
        return getBookShelfFragment().pagerAdapter.readingBookShelfFragment
    }

    private fun observeEventFlow() {
        lifecycleScope.launch {
            wishBookTapDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    private fun handleEvent(event: WishBookEvent) = when(event){
        is WishBookEvent.RemoveItem -> {
            returnEvent = WishBookEvent.RemoveItem
        }
        is WishBookEvent.AddItem -> {
            returnEvent = null
        }
        is WishBookEvent.MoveToReadingBook -> {
            returnEvent = WishBookEvent.MoveToReadingBook
            this.dismiss()
        }
    }

    override fun onDetach() {
        when(returnEvent){
            WishBookEvent.RemoveItem ->{
                val itemRemoveEvent = BookShelfViewModel.PagingViewEvent.Remove(bookShelfDataItem)
                bookShelfViewModel.addPagingViewEvent(itemRemoveEvent,ReadingStatus.WISH)
            }
            WishBookEvent.MoveToReadingBook -> {
                val itemRemoveEvent = BookShelfViewModel.PagingViewEvent.Remove(bookShelfDataItem)
                bookShelfViewModel.addPagingViewEvent(itemRemoveEvent,ReadingStatus.WISH)
                val bookShelfUiEvent = BookShelfEvent.ChangeBookShelfTab(READING_TAB_INDEX)
                bookShelfViewModel.startBookShelfUiEvent(bookShelfUiEvent)
                if(bookShelfViewModel.isReadingBookLoaded){
                    getReadingBookTabFragment().readingBookShelfDataAdapter.refresh()
                }
            }
            else -> { }
        }
        super.onDetach()
    }
}