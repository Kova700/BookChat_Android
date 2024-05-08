package com.example.bookchat.ui.bookshelf.reading.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogPageInputBottomSheetBinding
import com.example.bookchat.utils.makeToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PageInputBottomSheetDialog : BottomSheetDialogFragment() {

	private var _binding: DialogPageInputBottomSheetBinding? = null
	private val binding get() = _binding!!

	private val pageInputDialogViewModel: PageInputDialogViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.dialog_page_input_bottom_sheet, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = pageInputDialogViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeUiEvent()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		pageInputDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun handleEvent(event: PageInputDialogEvent) = when (event) {
		is PageInputDialogEvent.CloseDialog -> dismiss()
		is PageInputDialogEvent.MakeToast -> makeToast(event.stringId)
	}

	companion object {
		const val EXTRA_PAGE_INPUT_ITEM_ID = "EXTRA_PAGE_INPUT_ITEM_ID"
	}

}