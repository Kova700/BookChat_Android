package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.subhostdelete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.SubHostManageViewModel
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.subhostdelete.adapter.SubHostDeleteItemAdapter
import com.kova700.bookchat.feature.channel.databinding.FragmentSubHostDeleteBinding
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
		_binding = FragmentSubHostDeleteBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeUiState()
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun initViewState() {
		initAdapter()
		initRcv()
		with(binding) {
			xBtn.setOnClickListener { subHostManageViewModel.onClickXBtn() }
			moveAddSubHostBtn.setOnClickListener { subHostManageViewModel.onClickMoveAddSubHost() }
		}
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