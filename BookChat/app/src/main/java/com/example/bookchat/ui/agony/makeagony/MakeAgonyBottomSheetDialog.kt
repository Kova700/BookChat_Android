package com.example.bookchat.ui.agony.makeagony

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.databinding.DialogMakeAgonyBottomSheetBinding
import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.ui.agony.makeagony.MakeAgonyUiState.UiState
import com.example.bookchat.ui.agony.makeagony.util.getTextColorHexInt
import com.example.bookchat.utils.showSnackBar
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
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogMakeAgonyBottomSheetBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeUiState()
		observeUiEvent()
		observeEnteredTitle()
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		makeAgonyDialogViewModel.uiState.collect { uiState ->
			setViewState(uiState)
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

	private fun initViewState() {
		with(binding) {
			submitBtn.setOnClickListener {
				makeAgonyDialogViewModel.onRegisterBtnClick()
			}
			whiteColorToggle.setOnClickListener {
				makeAgonyDialogViewModel.onColorBtnClick(AgonyFolderHexColor.WHITE)
			}
			blackColorToggle.setOnClickListener {
				makeAgonyDialogViewModel.onColorBtnClick(AgonyFolderHexColor.BLACK)
			}
			purpleColorToggle.setOnClickListener {
				makeAgonyDialogViewModel.onColorBtnClick(AgonyFolderHexColor.PURPLE)
			}
			mintColorToggle.setOnClickListener {
				makeAgonyDialogViewModel.onColorBtnClick(AgonyFolderHexColor.MINT)
			}
			greenColorToggle.setOnClickListener {
				makeAgonyDialogViewModel.onColorBtnClick(AgonyFolderHexColor.GREEN)
			}
			yellowColorToggle.setOnClickListener {
				makeAgonyDialogViewModel.onColorBtnClick(AgonyFolderHexColor.YELLOW)
			}
			orangeColorToggle.setOnClickListener {
				makeAgonyDialogViewModel.onColorBtnClick(AgonyFolderHexColor.ORANGE)
			}
		}
	}

	private fun setViewState(uiState: MakeAgonyUiState) {
		binding.progressBar.visibility =
			if (uiState.uiState == UiState.LOADING) View.VISIBLE else View.GONE
		binding.makeAgonyFolderCv.backgroundTintList =
			ColorStateList.valueOf(Color.parseColor(uiState.selectedColor.hexcolor))
		with(binding.agonyFolderTitleEt) {
			setTextColor(uiState.selectedColor.getTextColorHexInt())
			setHintTextColor(uiState.selectedColor.getTextColorHexInt())
		}
		with(binding) {
			with(whiteColorToggle) {
				isChecked = uiState.selectedColor == AgonyFolderHexColor.WHITE
				isEnabled = isChecked.not()
			}
			with(blackColorToggle) {
				isChecked = uiState.selectedColor == AgonyFolderHexColor.BLACK
				isEnabled = isChecked.not()
			}
			with(purpleColorToggle) {
				isChecked = uiState.selectedColor == AgonyFolderHexColor.PURPLE
				isEnabled = isChecked.not()
			}
			with(mintColorToggle) {
				isChecked = uiState.selectedColor == AgonyFolderHexColor.MINT
				isEnabled = isChecked.not()
			}
			with(greenColorToggle) {
				isChecked = uiState.selectedColor == AgonyFolderHexColor.GREEN
				isEnabled = isChecked.not()
			}
			with(yellowColorToggle) {
				isChecked = uiState.selectedColor == AgonyFolderHexColor.YELLOW
				isEnabled = isChecked.not()
			}
			with(orangeColorToggle) {
				isChecked = uiState.selectedColor == AgonyFolderHexColor.ORANGE
				isEnabled = isChecked.not()
			}
		}
	}

	private fun handleEvent(event: MakeAgonyUiEvent) {
		when (event) {
			is MakeAgonyUiEvent.MoveToBack -> dismiss()
			is MakeAgonyUiEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.root
			)
		}
	}
}