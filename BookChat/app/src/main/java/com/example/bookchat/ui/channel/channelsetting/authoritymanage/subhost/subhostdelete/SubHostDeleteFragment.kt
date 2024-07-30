package com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.subhostdelete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSubHostDeleteBinding
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.SubHostManageViewModel
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.subhostdelete.adapter.SubHostDeleteItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SubHostDeleteFragment : Fragment() {

	private var _binding: FragmentSubHostDeleteBinding? = null
	private val binding get() = _binding!!

	private val subHostManageViewModel: SubHostManageViewModel by activityViewModels()

	@Inject
	lateinit var subHostDeleteItemAdapter: SubHostDeleteItemAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.fragment_sub_host_delete, container, false
		)
		binding.lifecycleOwner = viewLifecycleOwner
		binding.viewModel = subHostManageViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeUiState()
		initAdapter()
		initRcv()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		subHostManageViewModel.uiState.collect { state ->
			subHostDeleteItemAdapter.submitList(state.channel.subHosts)
		}
	}

	private fun initAdapter() {
		subHostDeleteItemAdapter.onClickDeleteBtn = { position ->
			val item = subHostDeleteItemAdapter.currentList[position]
			subHostManageViewModel.onClickSubHostDeleteBtn(item)
		}
	}

	private fun initRcv() {
		with(binding.channelMemeberRcv) {
			adapter = subHostDeleteItemAdapter
			setHasFixedSize(true)
			layoutManager = LinearLayoutManager(requireContext())
		}
	}
}