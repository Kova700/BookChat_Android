package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ActivityBookReportBinding
import com.example.bookchat.ui.dialog.BookReportExitDialog
import com.example.bookchat.ui.dialog.CompleteTapBookDialog.Companion.EXTRA_BOOKREPORT_BOOK
import com.example.bookchat.viewmodel.BookReportViewModel
import com.example.bookchat.viewmodel.BookReportViewModel.BookReportUIEvent
import com.google.android.material.snackbar.Snackbar
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
        setBackPressedDispatcher()

        observeBookReportEvent()
    }

    private fun observeBookReportEvent() = lifecycleScope.launch{
        bookReportViewModel.eventFlow.collect{ event -> handleEvent(event) }
    }

    private fun handleEvent(event : BookReportUIEvent){
        when(event){
            is BookReportUIEvent.MoveToBack -> { onBackPressedDispatcher.onBackPressed() }
            is BookReportUIEvent.UnknownError -> { showSnackbar(R.string.message_error_else) }
        }
    }

    private fun getBook() : BookShelfItem {
        return intent.getSerializableExtra(EXTRA_BOOKREPORT_BOOK) as BookShelfItem
    }

    private fun setBackPressedDispatcher(){
        onBackPressedDispatcher.addCallback{
            if (!bookReportViewModel.isEditingStatus()){
                bookReportViewModel.initCachedData()
                finish()
                return@addCallback
            }

            if(bookReportViewModel.isNotChangedReport() || bookReportViewModel.isBookReportEmpty()) {
                bookReportViewModel.initCachedData()
                finish()
                return@addCallback
            }

            val dialog = BookReportExitDialog()
            dialog.show(this@BookReportActivity.supportFragmentManager, DIALOG_TAG_BOOK_REPORT_EXIT)
        }
    }

    private fun showSnackbar(textId :Int){
        Snackbar.make(binding.bookReportLayout, textId, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        bookReportViewModel.initCachedData()
        super.onDestroy()
    }

    companion object {
        private const val DIALOG_TAG_BOOK_REPORT_EXIT = "BookReportExitDialog"
    }

}