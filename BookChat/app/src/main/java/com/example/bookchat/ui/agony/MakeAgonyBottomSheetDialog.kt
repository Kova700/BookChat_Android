package com.example.bookchat.ui.agony

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogMakeAgonyBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MakeAgonyBottomSheetDialog : BottomSheetDialogFragment() {

	private var _binding: DialogMakeAgonyBottomSheetBinding? = null
	private val binding get() = _binding!!

	private val makeAgonyDialogViewModel: MakeAgonyDialogViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding =
			DataBindingUtil.inflate(
				inflater, R.layout.dialog_make_agony_bottom_sheet,
				container, false
			)
		binding.viewmodel = makeAgonyDialogViewModel
		binding.lifecycleOwner = this

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeUiState()
		observeUiEvent()
		observeEnteredTitle()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		makeAgonyDialogViewModel.uiState.collect { uiState ->
			binding.makeAgonyFolderCv.backgroundTintList =
				ColorStateList.valueOf(Color.parseColor(uiState.selectedColor.hexcolor))
		}
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		makeAgonyDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun observeEnteredTitle() {
		binding.agonyFolderTitleEt.addTextChangedListener { text: Editable? ->
			makeAgonyDialogViewModel.onTitleChanged(text.toString())
		}
	}

	private fun handleEvent(event: MakeAgonyUiEvent) {
		when (event) {
			is MakeAgonyUiEvent.MoveToBack -> this.dismiss()
		}
	}
}