package com.example.bookchat.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ActivityBookReportBinding
import com.example.bookchat.ui.dialog.CompleteTapBookDialog.Companion.EXTRA_BOOKREPORT_BOOK
import com.example.bookchat.viewmodel.BookReportViewModel
import com.example.bookchat.viewmodel.BookReportViewModel.BookReportUIEvent
import com.example.bookchat.viewmodel.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookReportActivity : AppCompatActivity() {

    @Inject
    lateinit var bookReportViewModelFactory: BookReportViewModel.AssistedFactory

    private lateinit var binding :ActivityBookReportBinding
    private val bookReportViewModel: BookReportViewModel by viewModels {
        BookReportViewModel.provideFactory(bookReportViewModelFactory, getBook())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_book_report)
        binding.lifecycleOwner = this
        binding.viewmodel = bookReportViewModel

        observeBookReportEvent()
    }

    private fun observeBookReportEvent() = lifecycleScope.launch{
        bookReportViewModel.eventFlow.collect{ event -> handleEvent(event) }
    }

    private fun handleEvent(event : BookReportUIEvent){
        when(event){
            is BookReportUIEvent.MoveToBack -> { finish() }
        }
    }

    private fun getBook() : BookShelfItem {
        return intent.getSerializableExtra(EXTRA_BOOKREPORT_BOOK) as BookShelfItem
    }

}