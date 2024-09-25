package com.kova700.bookchat.feature.bookshelf.complete.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.navigation.AgonyNavigator
import com.kova700.bookchat.core.navigation.BookReportNavigator
import com.kova700.bookchat.feature.bookshelf.databinding.DialogCompleteBookTapClickedBinding
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.dialog.DialogSizeManager
import com.kova700.bookchat.util.image.image.loadUrl
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

	@Inject
	lateinit var agonyNavigator: AgonyNavigator

	@Inject
	lateinit var bookReportNavigator: BookReportNavigator

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogCompleteBookTapClickedBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		completeBookTapDialogViewModel.uiState.collect { state -> setViewState(state) }
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		completeBookTapDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun setViewState(uiState: CompleteBookDialogUiState) {
		with(binding) {
			bookImg.loadUrl(uiState.completeItem.book.bookCoverImageUrl)
			selectedBookTitleTv.text = uiState.completeItem.book.title
			selectedBookAuthorsTv.text = uiState.completeItem.book.authorsString
			selectedBookPublishAtTv.text = uiState.completeItem.book.publishAt
			completeBookRatingBar.rating = uiState.completeItem.star?.value ?: 0f
		}
	}

	private fun initViewState() {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
		dialogSizeManager.setDialogSize(binding.completeDialogLayout)
		with(binding) {
			moveToAgonyBtn.setOnClickListener { completeBookTapDialogViewModel.onMoveToAgonyClick() }
			moveToBookReportBtn.setOnClickListener {
				completeBookTapDialogViewModel.onMoveToBookReportClick()
			}
			selectedBookTitleTv.isSelected = true
			selectedBookAuthorsTv.isSelected = true
			selectedBookPublishAtTv.isSelected = true
		}
	}

	private fun moveToAgony(bookShelfItemId: Long) {
		agonyNavigator.navigate(
			currentActivity = requireActivity(),
			bookshelfId = bookShelfItemId,
		)
	}

	private fun moveToBookReport(bookShelfItemId: Long) {
		bookReportNavigator.navigate(
			currentActivity = requireActivity(),
			bookshelfId = bookShelfItemId,
		)
	}

	private fun handleEvent(event: CompleteBookDialogEvent) = when (event) {
		is CompleteBookDialogEvent.MoveToAgony -> moveToAgony(event.bookShelfItemId)
		is CompleteBookDialogEvent.MoveToBookReport -> moveToBookReport(event.bookShelfItemId)
	}

	companion object {
		const val EXTRA_COMPLETE_BOOKSHELF_ITEM_ID = "EXTRA_WISH_BOOKSHELF_ITEM_ID"
	}
}