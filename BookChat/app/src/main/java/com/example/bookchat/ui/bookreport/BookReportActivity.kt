package com.example.bookchat.ui.bookreport

import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityBookReportBinding
import com.example.bookchat.ui.bookreport.BookReportUiState.UiState
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl
import com.example.bookchat.utils.makeToast
import com.example.bookchat.utils.showSnackBar
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
		binding = DataBindingUtil.setContentView(this, R.layout.activity_book_report)
		binding.lifecycleOwner = this
		binding.viewmodel = bookReportViewModel
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
		binding.bookReportEditLayout.bookReportTitleEt.addTextChangedListener { text: Editable? ->
			text.let { bookReportViewModel.onChangeTitle(it.toString()) }
		}
		binding.bookReportEditLayout.bookReportContentEt.addTextChangedListener { text: Editable? ->
			text.let { bookReportViewModel.onChangeContent(it.toString()) }
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
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
		binding.bookImg.loadUrl(uiState.bookshelfItem.book.bookCoverImageUrl)
		binding.bookReportBookTitleTv.isSelected = true
		binding.bookReportBookAuthorsTv.isSelected = true
	}

	private fun setViewVisibility(uiState: BookReportUiState) {
		binding.editStateGroup.visibility =
			if (uiState.uiState == UiState.REVISE || uiState.uiState == UiState.EMPTY) View.VISIBLE else View.INVISIBLE
		binding.successStateGroup.visibility =
			if (uiState.uiState == UiState.SUCCESS) View.VISIBLE else View.INVISIBLE
		binding.bookReportLoading.visibility =
			if (uiState.uiState == UiState.LOADING) View.VISIBLE else View.INVISIBLE
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

			is BookReportEvent.MakeToast -> makeToast(event.stringId)
			is BookReportEvent.ErrorEvent -> binding.bookReportLayout.showSnackBar(event.stringId)
			is BookReportEvent.UnknownErrorEvent -> binding.bookReportLayout.showSnackBar(event.message)
		}
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
			bookReportViewModel.onClickBackBtn()
		}
	}

	companion object {
		private const val DIALOG_TAG_WARNING_BOOK_REPORT = "BookReportWarningDialog"
	}

}