package com.example.bookchat.ui.bookreport

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityBookReportBinding
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.ui.bookreport.BookReportViewModel.BookReportUIEvent
import com.example.bookchat.ui.bookshelf.complete.dialog.CompleteBookDialog.Companion.EXTRA_BOOKREPORT_BOOK
import com.example.bookchat.utils.makeToast
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookReportActivity : AppCompatActivity() {

	@Inject
	lateinit var bookReportViewModelFactory: BookReportViewModel.AssistedFactory

	private lateinit var binding: ActivityBookReportBinding
	private val bookReportViewModel: BookReportViewModel by viewModels {
		BookReportViewModel.provideFactory(bookReportViewModelFactory, getBook())
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_book_report)
		binding.lifecycleOwner = this
		binding.viewmodel = bookReportViewModel
		setBackPressedDispatcher()
		observeBookReportEvent()
	}

	override fun onDestroy() {
		bookReportViewModel.initCachedData()
		super.onDestroy()
	}

	private fun observeBookReportEvent() = lifecycleScope.launch {
		bookReportViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun handleEvent(event: BookReportUIEvent) {
		when (event) {
			is BookReportUIEvent.MoveToBack -> onBackPressedDispatcher.onBackPressed()
			is BookReportUIEvent.UnknownError -> binding.bookReportLayout.showSnackBar(R.string.error_else)
			is BookReportUIEvent.ShowDeleteWarningDialog -> showWarningDialog()
			is BookReportUIEvent.MakeToast -> makeToast(event.stringId)
		}
	}

	private fun getBook(): BookShelfItem {
		return intent.getSerializableExtra(EXTRA_BOOKREPORT_BOOK) as BookShelfItem
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
			if (!bookReportViewModel.isEditingStatus()) {
				bookReportViewModel.initCachedData()
				finish()
				return@addCallback
			}

			if (bookReportViewModel.isNotChangedReport() || bookReportViewModel.isBookReportEmpty()) {
				bookReportViewModel.initCachedData()
				finish()
				return@addCallback
			}
			showWarningDialog()
		}
	}

	private fun showWarningDialog() {
		val dialog = BookReportWarningDialog()
		dialog.show(this@BookReportActivity.supportFragmentManager, DIALOG_TAG_WARNING_BOOK_REPORT)
	}

	companion object {
		private const val DIALOG_TAG_WARNING_BOOK_REPORT = "BookReportWarningDialog"
	}

}