package com.example.bookchat.ui.bookshelf.complete.dialog

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
import com.example.bookchat.databinding.DialogCompleteBookTapClickedBinding
import com.example.bookchat.ui.agony.AgonyActivity
import com.example.bookchat.ui.bookshelf.reading.dialog.ReadingBookDialog.Companion.EXTRA_AGONY_BOOKSHELF_ITEM_ID
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CompleteBookDialog : DialogFragment() {

	private var _binding: DialogCompleteBookTapClickedBinding? = null
	private val binding get() = _binding!!

	private val completeBookTapDialogViewModel: CompleteBookDialogViewModel by viewModels()

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.dialog_complete_book_tap_clicked,
			container, false
		)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = completeBookTapDialogViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		observeUiEvent()
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		completeBookTapDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
		dialogSizeManager.setDialogSize(binding.completeDialogLayout)
	}

	private fun moveToAgony(bookShelfListItemId: Long) {
		val intent = Intent(requireContext(), AgonyActivity::class.java)
			.putExtra(EXTRA_AGONY_BOOKSHELF_ITEM_ID, bookShelfListItemId)
		startActivity(intent)
	}

	private fun moveToBookReport() {
//		val intent = Intent(requireContext(), BookReportActivity::class.java)
//			.putExtra(EXTRA_BOOKREPORT_BOOK, bookShelfListItem)
//		startActivity(intent)
	}

	private fun handleEvent(event: CompleteBookDialogEvent) = when (event) {
		is CompleteBookDialogEvent.MoveToAgony -> moveToAgony(event.bookShelfListItemId)
		is CompleteBookDialogEvent.MoveToBookReport -> moveToBookReport()
	}

	companion object {
		const val EXTRA_BOOKREPORT_BOOK = "EXTRA_BOOKREPORT_BOOK"
		const val EXTRA_COMPLETE_BOOKSHELF_ITEM_ID = "EXTRA_WISH_BOOKSHELF_ITEM_ID"
	}
}