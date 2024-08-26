package com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.subhostadd

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.databinding.FragmentSubHostAddBinding
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.adapter.MemberItemAdapter
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.SubHostManageUiState
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.SubHostManageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SubHostAddFragment : Fragment() {

	private var _binding: FragmentSubHostAddBinding? = null
	private val binding get() = _binding!!

	private val subHostManageViewModel: SubHostManageViewModel by activityViewModels()

	@Inject
	lateinit var memberItemAdapter: MemberItemAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentSubHostAddBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeUiState()
		initAdapter()
		initRcv()
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		subHostManageViewModel.uiState.collect { state ->
			memberItemAdapter.submitList(state.searchedMembers)
			setViewState(state)
		}
	}

	private fun initAdapter() {
		memberItemAdapter.onClick = { position ->
			val item = memberItemAdapter.currentList[position]
			subHostManageViewModel.onClickSubHostAddItem(item)
		}
	}

	private fun initRcv() {
		with(binding.channelMemeberRcv) {
			adapter = memberItemAdapter
			setHasFixedSize(true)
			layoutManager = LinearLayoutManager(requireContext())
		}
	}

	private fun initViewState() {
		with(binding.searchEditText) {
			addTextChangedListener { text ->
				val keyword = text?.toString() ?: return@addTextChangedListener
				subHostManageViewModel.onChangeSearchKeyWord(keyword)
			}
		}
		with(binding){
			xBtn.setOnClickListener { subHostManageViewModel.onClickMoveDeleteSubHost() }
			applyChannelChange.setOnClickListener { subHostManageViewModel.onClickApplyBtn() }
			searchDeleteBtn.setOnClickListener { subHostManageViewModel.onClickKeywordClearBtn() }
		}
	}

	private fun setViewState(state: SubHostManageUiState) {
		setSearchEtState(state)
		setSearchKeywordClearBtnState(state)
		setApplyBtnState(state)
	}

	private fun setSearchEtState(state: SubHostManageUiState) {
		with(binding.searchEditText) {
			if (state.searchKeyword != text.toString()) {
				setText(state.searchKeyword)
				setSelection(state.searchKeyword.length)
			}
		}
	}

	private fun setSearchKeywordClearBtnState(state: SubHostManageUiState) {
		with(binding.searchDeleteBtn) {
			visibility = if (state.searchKeyword.isBlank()) View.INVISIBLE else View.VISIBLE
		}
	}

	private fun setApplyBtnState(state: SubHostManageUiState) {
		with(binding.applyChannelChange) {
			if (state.isExistSelectedMember) {
				setTextColor(Color.parseColor("#000000"))
				isEnabled = true
			} else {
				setTextColor(Color.parseColor("#D9D9D9"))
				isEnabled = false
			}
		}
	}

}