package com.example.bookchat.ui.bookreport

import android.os.Bundle
import android.text.Editable
import android.util.Log
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
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.makeToast
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookReportActivity : AppCompatActivity() {

	private lateinit var binding: ActivityBookReportBinding
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
			Log.d(TAG, "BookReportActivity: observeUiState() - state :$state")
			setViewState(state)
			setViewVisibility(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		bookReportViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		binding.bookReportTitleEt.addTextChangedListener { text: Editable? ->
			text.let { bookReportViewModel.onChangeTitle(it.toString()) }
		}
		binding.bookReportContentEt.addTextChangedListener { text: Editable? ->
			text.let { bookReportViewModel.onChangeContent(it.toString()) }
		}
	}

	private fun setViewState(uiState: BookReportUiState) {
		with(binding.bookReportTitleEt) {
			if (uiState.enteredTitle != text.toString()) {
				setText(uiState.enteredTitle)
				setSelection(uiState.enteredTitle.length)
			}
		}
		with(binding.bookReportContentEt) {
			if (uiState.enteredContent != text.toString()) {
				setText(uiState.enteredContent)
				setSelection(uiState.enteredContent.length)
			}
		}
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
		onOkClick: () -> Unit
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