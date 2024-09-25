package com.kova700.bookchat.feature.bookreport

import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.feature.bookreport.BookReportUiState.UiState
import com.kova700.bookchat.feature.bookreport.databinding.ActivityBookReportBinding
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.image.image.loadUrl
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookReportActivity : AppCompatActivity() {

	private lateinit var binding: ActivityBookReportBinding

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	private val bookReportViewModel by viewModels<BookReportViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityBookReportBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setBackPressedDispatcher()
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		bookReportViewModel.uiState.collect { state ->
			setViewState(state)
			setViewVisibility(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		bookReportViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
		with(binding) {
			backBtn.setOnClickListener { bookReportViewModel.onClickBackBtn() }
			reviseBtn.setOnClickListener { bookReportViewModel.onClickReviseBtn() }
			deleteBtn.setOnClickListener { bookReportViewModel.onClickDeleteBtn() }
			registerBtn.setOnClickListener { bookReportViewModel.onClickRegisterBtn() }
			bookReportEditLayout.bookReportTitleEt.addTextChangedListener { text: Editable? ->
				text.let { bookReportViewModel.onChangeTitle(it.toString()) }
			}
			bookReportEditLayout.bookReportContentEt.addTextChangedListener { text: Editable? ->
				text.let { bookReportViewModel.onChangeContent(it.toString()) }
			}
		}
	}

	private fun setViewState(uiState: BookReportUiState) {
		with(binding.bookReportEditLayout.bookReportTitleEt) {
			if (uiState.enteredTitle != text.toString()) {
				setText(uiState.enteredTitle)
				setSelection(uiState.enteredTitle.length)
			}
		}
		with(binding.bookReportEditLayout.bookReportContentEt) {
			if (uiState.enteredContent != text.toString()) {
				setText(uiState.enteredContent)
				setSelection(uiState.enteredContent.length)
			}
		}
		with(binding.bookReportDefaultLayout) {
			bookReportTitleTv.text = uiState.existingBookReport.reportTitle
			bookReportContentTv.text = uiState.existingBookReport.reportContent
			bookReportCreatedDateTv.text = uiState.existingBookReport.reportCreatedAt
		}
		with(binding) {
			bookImg.loadUrl(uiState.bookshelfItem.book.bookCoverImageUrl)
			bookTitleTv.text = uiState.bookshelfItem.book.title
			bookTitleTv.isSelected = true
			bookAuthorsTv.text = uiState.bookshelfItem.book.authorsString
			bookAuthorsTv.isSelected = true
			starRatingBar.rating = uiState.bookshelfItem.star?.value ?: 0F
		}
	}

	private fun setViewVisibility(uiState: BookReportUiState) {
		with(binding) {
			editStateGroup.visibility =
				if (uiState.isEditOrEmpty) View.VISIBLE else View.GONE
			successStateGroup.visibility =
				if (uiState.uiState == UiState.SUCCESS) View.VISIBLE else View.GONE
			progressbar.visibility =
				if (uiState.uiState == UiState.LOADING) View.VISIBLE else View.GONE
		}
	}

	private fun showWarningDialog(
		warningTextStringId: Int,
		onOkClick: () -> Unit,
	) {
		val dialog = BookReportWarningDialog(
			warningTextStringId = warningTextStringId,
			onOkClick = onOkClick
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_WARNING_BOOK_REPORT)
	}

	private fun handleEvent(event: BookReportEvent) {
		when (event) {
			is BookReportEvent.MoveBack -> finish()
			is BookReportEvent.ShowDeleteWarningDialog -> showWarningDialog(
				warningTextStringId = event.stringId,
				onOkClick = event.onOkClick
			)

			is BookReportEvent.ShowSnackBar -> binding.root.showSnackBar(event.stringId)
			is BookReportEvent.UnknownErrorEvent -> binding.root.showSnackBar(event.message)
		}
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
			bookReportViewModel.onClickBackBtn()
		}
	}

	companion object {
		private const val DIALOG_TAG_WARNING_BOOK_REPORT = "BookReportWarningDialog"
		internal const val EXTRA_BOOKREPORT_BOOKSHELF_ITEM_ID = "EXTRA_BOOKREPORT_BOOKSHELF_ITEM_ID"
	}

}