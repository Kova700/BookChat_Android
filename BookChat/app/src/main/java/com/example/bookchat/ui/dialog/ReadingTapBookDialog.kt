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
import com.example.bookchat.databinding.DialogReadingBookTapClickedBinding
import com.example.bookchat.ui.activity.AgonyActivity
import com.example.bookchat.ui.fragment.BookShelfFragment
import com.example.bookchat.ui.fragment.CompleteBookTabFragment
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.ReadingBookTapDialogViewModel
import com.example.bookchat.viewmodel.ReadingBookTapDialogViewModel.ReadingBookEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReadingTapBookDialog(private val book: BookShelfItem) : DialogFragment() {

    @Inject
    lateinit var readingBookTapDialogViewModelFactory :ReadingBookTapDialogViewModel.AssistedFactory

    private lateinit var binding : DialogReadingBookTapClickedBinding
    private val readingBookTapDialogViewModel : ReadingBookTapDialogViewModel by viewModels{
        ReadingBookTapDialogViewModel.provideFactory(readingBookTapDialogViewModelFactory, book)
    }
    private val bookShelfViewModel: BookShelfViewModel by viewModels({ getBookShelfFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.dialog_reading_book_tap_clicked,container,false)
        binding.lifecycleOwner = this
        binding.viewmodel = readingBookTapDialogViewModel
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        observeEventFlow()

        return binding.root
    }

    private fun observeEventFlow() {
        lifecycleScope.launch{
            readingBookTapDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    private fun getBookShelfFragment() : BookShelfFragment {
        var fragment = requireParentFragment()
        while (fragment !is BookShelfFragment){
            fragment = fragment.requireParentFragment()
        }
        return fragment
    }

    private fun getCompleteBookTabFragment() : CompleteBookTabFragment {
        return getBookShelfFragment().pagerAdapter.completeBookTabFragment
    }

    private fun handleEvent(event : ReadingBookEvent) = when(event){
        is ReadingBookEvent.OpenAgonize -> { openAgonizeActivity() }

        is ReadingBookEvent.MoveToCompleteBook -> {
            val removeEvent = BookShelfViewModel.PagingViewEvent.Remove(book)
            bookShelfViewModel.onPagingViewEvent(removeEvent, ReadingStatus.READING)
            bookShelfViewModel.startEvent(BookShelfViewModel.BookShelfEvent.ChangeBookShelfTab(2))
            if(bookShelfViewModel.isCompleteBookLoaded){
                getCompleteBookTabFragment().completeBookAdapter.refresh()
            }
            this.dismiss()
        }
    }

    private fun openAgonizeActivity(){
        val intent = Intent(requireContext(), AgonyActivity::class.java)
            .putExtra(EXTRA_AGONIZE_BOOK,book)
        startActivity(intent)
    }

    override fun onDestroyView() {
        readingBookTapDialogViewModel.starRating.value = 0F
        super.onDestroyView()
    }

    companion object {
        const val EXTRA_AGONIZE_BOOK = "EXTRA_AGONIZE_BOOK"
    }
}