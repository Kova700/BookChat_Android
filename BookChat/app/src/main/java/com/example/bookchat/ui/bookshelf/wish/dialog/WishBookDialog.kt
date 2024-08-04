package com.example.bookchat.ui.bookshelf.wish.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.databinding.DialogWishBookTapClickedBinding
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.agony.agony.AgonyActivity
import com.example.bookchat.ui.agony.agony.AgonyViewModel
import com.example.bookchat.ui.bookshelf.wish.WishBookShelfViewModel
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.DialogSizeManager
import com.example.bookchat.utils.image.loadUrl
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WishBookDialog : DialogFragment() {

	private var _binding: DialogWishBookTapClickedBinding? = null
	private val binding get() = _binding!!

	private val wishBookDialogViewModel: WishBookDialogViewModel by viewModels()
	private val wishBookShelfViewModel: WishBookShelfViewModel by viewModels({
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
	): View {
		_binding = DialogWishBookTapClickedBinding.inflate(inflater, container, false)
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
		wishBookDialogViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		wishBookDialogViewModel.eventFlow.collect(::handleEvent)
	}

	private fun setViewState(uiState: WishBookDialogUiState) {
		setViewVisibility(uiState)
		with(binding) {
			bookImg.loadUrl(uiState.wishItem.book.bookCoverImageUrl)
			bookTitleTv.text = uiState.wishItem.book.title
			bookTitleTv.isSelected = true
			bookAuthorsTv.text = uiState.wishItem.book.authorsString
			bookAuthorsTv.isSelected = true
			bookPublishAtTv.text = uiState.wishItem.book.publishAt
			bookPublishAtTv.isSelected = true
		}
	}

	private fun setViewVisibility(uiState: WishBookDialogUiState) {
		with(binding) {
			progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
			notLoadingStateGroup.visibility =
				if (uiState.isLoading.not()) View.VISIBLE else View.INVISIBLE
		}
	}

	private fun initViewState() {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
		dialogSizeManager.setDialogSize(binding.wishDialogLayout)
		with(binding) {
			wishHeartToggleBtn.setOnClickListener {
				wishHeartToggleBtn.isChecked = true
				wishBookDialogViewModel.onHeartToggleClick()
			}
			moveToAgonyBtn.setOnClickListener { wishBookDialogViewModel.onMoveToAgonyClick() }
			changeStatusToReadingBtn.setOnClickListener { wishBookDialogViewModel.onChangeToReadingClick() }
		}
	}

	private fun moveToOtherTab(targetState: BookShelfState) {
		wishBookShelfViewModel.moveToOtherTab(targetState)
		dismiss()
	}

	private fun moveToAgony(bookShelfListItemId: Long) {
		val intent = Intent(requireContext(), AgonyActivity::class.java)
			.putExtra(AgonyViewModel.EXTRA_AGONY_BOOKSHELF_ITEM_ID, bookShelfListItemId)
		startActivity(intent)
	}

	private fun handleEvent(event: WishBookDialogEvent) {
		when (event) {
			is WishBookDialogEvent.ChangeBookShelfTab -> moveToOtherTab(event.targetState)
			is WishBookDialogEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.moveToAgonyBtn
			)

			is WishBookDialogEvent.MoveToAgony -> moveToAgony(event.bookShelfListItemId)
			WishBookDialogEvent.MoveToBack -> dismiss()
		}
	}

	companion object {
		const val EXTRA_WISH_BOOKSHELF_ITEM_ID = "EXTRA_WISH_BOOKSHELF_ITEM_ID"
	}
}