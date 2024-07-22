package com.example.bookchat.ui.bookshelf.reading.dialog

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
import com.example.bookchat.databinding.DialogReadingBookTapClickedBinding
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.agony.agony.AgonyActivity
import com.example.bookchat.ui.agony.agony.AgonyViewModel
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfViewModel
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.DialogSizeManager
import com.example.bookchat.utils.image.loadUrl
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReadingBookDialog : DialogFragment() {

	private var _binding: DialogReadingBookTapClickedBinding? = null
	private val binding get() = _binding!!

	private val readingBookDialogViewModel: ReadingBookDialogViewModel by viewModels()
	private val readingBookShelfViewModel: ReadingBookShelfViewModel by viewModels({
		requireParentFragment()
	})

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.dialog_reading_book_tap_clicked, container, false)
		binding.lifecycleOwner = viewLifecycleOwner
		binding.viewmodel = readingBookDialogViewModel
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
		readingBookDialogViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		readingBookDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		binding.readingBookRatingBar.setOnRatingChangeListener { ratingBar, rating, fromUser ->
			readingBookDialogViewModel.onChangeStarRating(rating)
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
		dialogSizeManager.setDialogSize(binding.readingDialogLayout)
	}

	private fun setViewState(state: ReadingBookDialogUiState) {
		if (binding.readingBookRatingBar.rating != state.starRating) {
			binding.readingBookRatingBar.rating = state.starRating
		}

		with(binding.changeStatusToCompleteBtn) {
			if (state.haveStar) {
				setBackgroundColor(Color.parseColor("#5648FF"))
				isEnabled = true
				return
			}
			setBackgroundColor(Color.parseColor("#D9D9D9"))
			isEnabled = false
		}

		binding.bookImg.loadUrl(state.readingItem.book.bookCoverImageUrl)
		binding.selectedBookTitleTv.isSelected = true
		binding.selectedBookAuthorsTv.isSelected = true
		binding.selectedBookPublishAtTv.isSelected = true
	}

	private fun moveToAgony(bookShelfListItemId: Long) {
		val intent = Intent(requireContext(), AgonyActivity::class.java)
			.putExtra(AgonyViewModel.EXTRA_AGONY_BOOKSHELF_ITEM_ID, bookShelfListItemId)
		startActivity(intent)
	}

	private fun moveToOtherTab(targetState: BookShelfState) {
		readingBookShelfViewModel.moveToOtherTab(targetState)
		dismiss()
	}

	private fun handleEvent(event: ReadingBookDialogEvent) = when (event) {
		is ReadingBookDialogEvent.MoveToAgony -> moveToAgony(event.bookShelfListItemId)
		is ReadingBookDialogEvent.ChangeBookShelfTab -> moveToOtherTab(event.targetState)
		is ReadingBookDialogEvent.MakeToast -> makeToast(event.stringId)
	}

	companion object {
		const val EXTRA_READING_BOOKSHELF_ITEM_ID = "EXTRA_READING_BOOKSHELF_ITEM_ID"
	}
}