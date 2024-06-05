package com.example.bookchat.ui.signup.selecttaste

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySelectTasteBinding
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectTasteActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySelectTasteBinding
	private val selectTasteViewModel: SelectTasteViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_select_taste)
		binding.lifecycleOwner = this
		binding.viewModel = selectTasteViewModel
		observeUiState()
		observeUiEvent()
	}

	private fun observeUiState() = lifecycleScope.launch {
		selectTasteViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		selectTasteViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun setViewState(uiState: SelectTasteState) {
		setStartButtonState(uiState)
	}

	private fun setStartButtonState(uiState: SelectTasteState) {
		with(binding.startBookchatBtn) {
			if (uiState.readingTastes.isEmpty()) {
				setBackgroundColor(Color.parseColor("#D9D9D9"))
				isEnabled = false
				return
			}
			setBackgroundColor(Color.parseColor("#5648FF"))
			isEnabled = true
		}
	}

	private fun moveToMain() {
		val intent = Intent(this, MainActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(intent)
		finish()
	}

	private fun handleEvent(event: SelectTasteEvent) = when (event) {
		is SelectTasteEvent.MoveToBack -> finish()
		is SelectTasteEvent.MoveToMain -> moveToMain()

		is SelectTasteEvent.ErrorEvent -> binding.selectTasteLayout.showSnackBar(event.stringId)
		is SelectTasteEvent.UnknownErrorEvent -> binding.selectTasteLayout.showSnackBar(event.message)
	}
}