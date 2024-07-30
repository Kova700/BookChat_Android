package com.example.bookchat.ui.channel.channelsetting.authoritymanage.host

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.databinding.ActivityHostManageBinding
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.adapter.MemberItemAdapter
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.host.dialog.HostChangeSuccessDialog
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HostManageActivity : AppCompatActivity() {

	private lateinit var binding: ActivityHostManageBinding

	private val hostManageViewModel: HostManageViewModel by viewModels()

	@Inject
	lateinit var memberItemAdapter: MemberItemAdapter

	private val imm by lazy {
		getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityHostManageBinding.inflate(layoutInflater)
		setContentView(binding.root)
		observeUiState()
		observeUiEvent()
		initRcv()
		initAdapter()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		hostManageViewModel.uiState.collect { state ->
			memberItemAdapter.submitList(state.searchedMembers)
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		hostManageViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initAdapter() {
		memberItemAdapter.onClick = { position ->
			val item = memberItemAdapter.currentList[position]
			hostManageViewModel.onClickMember(item)
		}
	}

	private fun initRcv() {
		with(binding.channelMemeberRcv) {
			adapter = memberItemAdapter
			setHasFixedSize(true)
			layoutManager = LinearLayoutManager(this@HostManageActivity)
		}
	}

	private fun initViewState() {
		with(binding.searchEditText) {
			addTextChangedListener { text ->
				val keyword = text?.toString() ?: return@addTextChangedListener
				hostManageViewModel.onChangeSearchKeyWord(keyword)
			}
		}
		with(binding) {
			xBtn.setOnClickListener { hostManageViewModel.onClickXBtn() }
			applyBtn.setOnClickListener { hostManageViewModel.onClickApplyBtn() }
			searchKeywordClearBtn.setOnClickListener { hostManageViewModel.onClickKeywordClearBtn() }
		}
	}

	private fun setViewState(state: HostManageUiState) {
		setSearchEtState(state)
		setSearchKeywordClearBtnState(state)
		setApplyBtnState(state)
	}

	private fun setSearchEtState(state: HostManageUiState) {
		with(binding.searchEditText) {
			if (state.searchKeyword != text.toString()) {
				setText(state.searchKeyword)
				setSelection(state.searchKeyword.length)
			}
		}
	}

	private fun closeKeyboard() {
		imm.hideSoftInputFromWindow(
			binding.root.windowToken,
			InputMethodManager.HIDE_NOT_ALWAYS
		)
	}

	private fun setSearchKeywordClearBtnState(state: HostManageUiState) {
		with(binding.searchKeywordClearBtn) {
			visibility = if (state.searchKeyword.isBlank()) View.INVISIBLE else View.VISIBLE
		}
	}

	private fun setApplyBtnState(state: HostManageUiState) {
		with(binding.applyBtn) {
			if (state.isExistSelectedMember) {
				setTextColor(Color.parseColor("#000000"))
				isEnabled = true
			} else {
				setTextColor(Color.parseColor("#D9D9D9"))
				isEnabled = false
			}
		}
	}

	private fun showHostChangeSuccessDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_HOST_CHANGE_SUCCESS)
		if (existingFragment != null) return
		val dialog = HostChangeSuccessDialog(
			onClickOk = {
				setResult(RESULT_OK, intent)
				finish()
			}
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_HOST_CHANGE_SUCCESS)
	}

	private fun handleEvent(event: HostManageUiEvent) {
		when (event) {
			is HostManageUiEvent.ShowSnackBar -> binding.root.showSnackBar(textId = event.stringId)
			HostManageUiEvent.MoveBack -> finish()
			HostManageUiEvent.ShowHostChangeSuccessDialog -> showHostChangeSuccessDialog()
			HostManageUiEvent.CloseKeyboard -> closeKeyboard()
		}
	}

	companion object {
		const val DIALOG_TAG_HOST_CHANGE_SUCCESS = "DIALOG_TAG_HOST_CHANGE_SUCCESS"
	}
}