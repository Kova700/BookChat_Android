package com.example.bookchat.ui.agony.agonyedit

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityAgonyEditBinding
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AgonyEditActivity : AppCompatActivity() {
	private lateinit var binding: ActivityAgonyEditBinding
	private val agonyEditViewModel: AgonyEditViewModel by viewModels()
	private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityAgonyEditBinding.inflate(layoutInflater)
		setContentView(binding.root)
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		agonyEditViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		agonyEditViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		initAgonyTitleEt()
		with(binding) {
			xBtn.setOnClickListener { agonyEditViewModel.onClickXBtn() }
			agonyEditConfirmBtn.setOnClickListener { agonyEditViewModel.onClickConfirmBtn() }
			clearTitleBtn.setOnClickListener { agonyEditViewModel.onClickClearTitleBtn() }
		}
	}

	private fun initAgonyTitleEt() {
		with(binding.agonyTitleEt) {
			setFocus()
			addTextChangedListener { text ->
				agonyEditViewModel.onChangeNewTitle(text?.toString() ?: return@addTextChangedListener)
			}
		}
	}

	private fun setViewState(state: AgonyEditUiState) {
		setAgonyEditConfirmBtnState(state)
		setAgonyTitleEtState(state)
		binding.newTitleLengthTv.text =
			getString(R.string.agony_title_new_title_length, state.newTitle.length)
	}

	private fun setAgonyEditConfirmBtnState(state: AgonyEditUiState) {
		with(binding.agonyEditConfirmBtn) {
			if (state.isPossibleChangeAgony) {
				isEnabled = true
				setTextColor(context.getColor(R.color.black))
			} else {
				isEnabled = false
				setTextColor(context.getColor(R.color.bookchat_white_gray))
			}
		}
	}

	private fun setAgonyTitleEtState(state: AgonyEditUiState) {
		with(binding.agonyTitleEt) {
			if (state.newTitle != text.toString()) {
				setText(state.newTitle)
				setSelection(state.newTitle.length)
			}
		}
	}

	private fun setFocus() {
		binding.agonyTitleEt.requestFocus()
		openKeyboard(binding.agonyTitleEt)
	}

	private fun openKeyboard(view: View) {
		Handler(Looper.getMainLooper()).postDelayed({
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
		}, KEYBOARD_DELAY_TIME)
	}

	private fun closeKeyboard() {
		binding.agonyTitleEt.clearFocus()
		imm.hideSoftInputFromWindow(
			binding.agonyTitleEt.windowToken,
			InputMethodManager.HIDE_NOT_ALWAYS
		)
	}

	private fun handleEvent(event: AgonyEditUiEvent) {
		when (event) {
			is AgonyEditUiEvent.ShowSnackBar -> binding.root.showSnackBar(textId = event.stringId)
			AgonyEditUiEvent.MoveToBack -> finish()
			AgonyEditUiEvent.CloseKeyboard -> closeKeyboard()
		}
	}

	companion object {
		private const val KEYBOARD_DELAY_TIME = 200L
	}
}