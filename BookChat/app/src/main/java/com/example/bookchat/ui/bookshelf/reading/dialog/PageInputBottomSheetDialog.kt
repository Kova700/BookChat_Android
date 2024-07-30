package com.example.bookchat.ui.bookshelf.reading.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.databinding.DialogPageInputBottomSheetBinding
import com.example.bookchat.ui.bookshelf.reading.dialog.PageInputDialogUiState.UiState
import com.example.bookchat.utils.showSnackBar
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
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogPageInputBottomSheetBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initViewState()
		observeUiState()
		observeUiEvent()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		pageInputDialogViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		pageInputDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		with(binding) {
			inputButtonOne.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(1) }
			inputButtonTwo.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(2) }
			inputButtonThree.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(3) }
			inputButtonFour.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(4) }
			inputButtonFive.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(5) }
			inputButtonSix.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(6) }
			inputButtonSeven.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(7) }
			inputButtonEight.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(8) }
			inputButtonNine.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(9) }
			inputButtonZero.setOnClickListener { pageInputDialogViewModel.onClickNumberBtn(0) }
			deleteButton.setOnClickListener { pageInputDialogViewModel.onClickDeleteNumberBtn() }
			submitButton.setOnClickListener { pageInputDialogViewModel.onClickSubmit() }
		}
	}

	private fun setViewState(uiState: PageInputDialogUiState) {
		binding.inputedPageEt.setText(uiState.inputPage)
		binding.progressBar.visibility =
			if (uiState.uiState == UiState.LOADING) View.VISIBLE else View.GONE
	}

	private fun handleEvent(event: PageInputDialogEvent) = when (event) {
		is PageInputDialogEvent.CloseDialog -> dismiss()
		is PageInputDialogEvent.ShowSnackBar -> binding.root.showSnackBar(
			textId = event.stringId,
			anchor = binding.root
		)
	}

	companion object {
		const val EXTRA_PAGE_INPUT_ITEM_ID = "EXTRA_PAGE_INPUT_ITEM_ID"
	}

}