package com.example.bookchat.ui.agony.agonyedit

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityAgonyEditBinding
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AgonyEditActivity : AppCompatActivity() {
	private lateinit var binding: ActivityAgonyEditBinding
	private val agonyEditViewModel: AgonyEditViewModel by viewModels()
	private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_agony_edit)
		binding.lifecycleOwner = this
		binding.viewmodel = agonyEditViewModel
		setFocus()
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
		with(binding.agonyTitleEt) {
			addTextChangedListener { text ->
				val newTitle = text?.toString() ?: return@addTextChangedListener
				agonyEditViewModel.onChangeNewTitle(newTitle)
			}
		}
	}

	private fun setViewState(state: AgonyEditUiState) {
		with(binding.agonyEditConfirmBtn) {
			if (state.isPossibleChangeAgony) {
				setTextColor(Color.parseColor("#000000"))
				isEnabled = true
			} else {
				setTextColor(Color.parseColor("#D9D9D9"))
				isEnabled = false
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

	private fun handleEvent(event: AgonyEditUiEvent) {
		when (event) {
			is AgonyEditUiEvent.MakeToast -> makeToast(event.stringId)
			AgonyEditUiEvent.MoveToBack -> finish()
		}
	}

	companion object {
		private const val KEYBOARD_DELAY_TIME = 200L
	}
}