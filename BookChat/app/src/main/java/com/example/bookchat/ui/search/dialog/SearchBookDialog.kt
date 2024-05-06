package com.example.bookchat.ui.search.dialog

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
import com.example.bookchat.databinding.DialogSearchBookClickedBinding
import com.example.bookchat.ui.search.dialog.SearchDialogUiState.SearchDialogState
import com.example.bookchat.ui.search.dialog.SearchDialogUiState.SearchDialogState.AlreadyInBookShelf
import com.example.bookchat.ui.search.dialog.SearchDialogUiState.SearchDialogState.Default
import com.example.bookchat.ui.search.dialog.SearchDialogUiState.SearchDialogState.Loading
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchBookDialog : DialogFragment() {

	private var _binding: DialogSearchBookClickedBinding? = null
	private val binding get() = _binding!!

	private val searchBookDialogViewModel: SearchBookDialogViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.dialog_search_book_clicked, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = searchBookDialogViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		observeUiState()
		observeEvent()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		searchBookDialogViewModel.uiState.collect { state ->
			setViewVisibility(state.uiState)
		}
	}

	private fun observeEvent() = viewLifecycleOwner.lifecycleScope.launch {
		searchBookDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun setViewVisibility(uiState: SearchDialogState) {
		with(binding) {
			loadingProgressbar.visibility =
				if (uiState == Loading) View.VISIBLE else View.INVISIBLE
			notLoadingStateGroup.visibility =
				if (uiState == Loading) View.INVISIBLE else View.VISIBLE
			alreadyInBookshelfBtn.visibility =
				if (uiState is AlreadyInBookShelf) View.VISIBLE else View.INVISIBLE
			changeStatusToCompleteBtn.visibility =
				if (uiState is Default) View.VISIBLE else View.INVISIBLE
			changeStatusToReadingBtn.visibility =
				if (uiState is Default) View.VISIBLE else View.INVISIBLE
			heartBtn.visibility =
				if (uiState is Default) View.VISIBLE else View.INVISIBLE
		}
	}

	private fun moveToStarSetDialog() {
		val dialog = CompleteBookStarSetDialog()
		dialog.show(childFragmentManager, DIALOG_TAG_STAR_SET)
	}

	private fun handleEvent(event: SearchTapDialogEvent) = when (event) {
		is SearchTapDialogEvent.MoveToStarSetDialog -> moveToStarSetDialog()
		is SearchTapDialogEvent.MakeToast -> makeToast(event.stringId)
	}

	companion object {
		const val DIALOG_TAG_STAR_SET = "DIALOG_TAG_STAR_SET"
	}

}