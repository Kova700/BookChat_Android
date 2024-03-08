package com.example.bookchat.ui.bookshelf.reading.dialog

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
import com.example.bookchat.databinding.DialogReadingBookTapClickedBinding
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReadingBookDialog : DialogFragment() {

	private var _binding: DialogReadingBookTapClickedBinding? = null
	private val binding get() = _binding!!

	private val readingBookDialogViewModel: ReadingBookDialogViewModel by viewModels()
	private val readingBookShelfViewModel: ReadingBookShelfViewModel by viewModels({
		requireParentFragment()
	})

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.dialog_reading_book_tap_clicked, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = readingBookDialogViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		observeUiEvent()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		readingBookDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun moveToAgony() {
//		val intent = Intent(requireContext(), AgonyActivity::class.java)
//			.putExtra(EXTRA_AGONIZE_BOOK, bookShelfListItem) //개선 필요
//		startActivity(intent)
	}

	private fun moveToOtherTab(targetState: BookShelfState) {
		readingBookShelfViewModel.moveToOtherTab(targetState)
		dismiss()
	}

	private fun handleEvent(event: ReadingBookDialogEvent) = when (event) {
		is ReadingBookDialogEvent.MoveToAgony -> moveToAgony()
		is ReadingBookDialogEvent.ChangeBookShelfTab -> moveToOtherTab(event.targetState)
	}

	companion object {
		const val EXTRA_AGONIZE_BOOK = "EXTRA_AGONIZE_BOOK"
		const val EXTRA_READING_BOOKSHELF_ITEM_ID = "EXTRA_READING_BOOKSHELF_ITEM_ID"
	}
}