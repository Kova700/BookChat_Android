package com.example.bookchat.ui.bookshelf.wish.dialog

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
import com.example.bookchat.databinding.DialogWishBookTapClickedBinding
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.wish.WishBookShelfViewModel
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.makeToast
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

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.dialog_wish_book_tap_clicked, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = wishBookDialogViewModel
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
		wishBookDialogViewModel.eventFlow.collect(::handleEvent)
	}

	private fun initViewState() {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	private fun moveToOtherTab(targetState: BookShelfState) {
		wishBookShelfViewModel.moveToOtherTab(targetState)
		dismiss()
	}

	private fun handleEvent(event: WishBookDialogEvent) = when (event) {
		is WishBookDialogEvent.ChangeBookShelfTab -> moveToOtherTab(event.targetState)
		is WishBookDialogEvent.MakeToast -> makeToast(event.stringId)
	}

	companion object {
		const val EXTRA_WISH_BOOKSHELF_ITEM_ID = "EXTRA_WISH_BOOKSHELF_ITEM_ID"
	}
}