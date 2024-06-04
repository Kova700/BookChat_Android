package com.example.bookchat.ui.channel.channelsetting.authoritymanage.host

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityHostManageBinding
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.adapter.MemberItemAdapter
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.host.dialog.HostChangeSuccessDialog
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HostManageActivity : AppCompatActivity() {

	private lateinit var binding: ActivityHostManageBinding

	private val hostManageViewModel: HostManageViewModel by viewModels()

	@Inject
	lateinit var memberItemAdapter: MemberItemAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_host_manage)
		binding.viewmodel = hostManageViewModel
		binding.lifecycleOwner = this
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

	private fun setSearchKeywordClearBtnState(state: HostManageUiState) {
		with(binding.searchDeleteBtn) {
			visibility = if (state.searchKeyword.isBlank()) View.INVISIBLE else View.VISIBLE
		}
	}

	private fun setApplyBtnState(state: HostManageUiState) {
		with(binding.applyChannelChangeTv) {
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
		val dialog = HostChangeSuccessDialog(onClickOk = {
			setResult(RESULT_OK, intent)
			finish()
		})
		dialog.show(supportFragmentManager, DIALOG_TAG_HOST_CHANGE_SUCCESS)
	}

	private fun handleEvent(event: HostManageUiEvent) = when (event) {
		is HostManageUiEvent.MakeToast -> makeToast(event.stringId)
		HostManageUiEvent.MoveBack -> finish()
		HostManageUiEvent.ShowHostChangeSuccessDialog -> showHostChangeSuccessDialog()
	}

	companion object {
		const val DIALOG_TAG_HOST_CHANGE_SUCCESS = "DIALOG_TAG_HOST_CHANGE_SUCCESS"
	}
}