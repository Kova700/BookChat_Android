package com.example.bookchat.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.MainBookItemDecoration
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.FragmentHomeBinding
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.activity.ChannelActivity
import com.example.bookchat.ui.adapter.home.MainBookAdapter
import com.example.bookchat.ui.adapter.userchatroomlist.ChannelListDataAdapter
import com.example.bookchat.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

//TODO : 독서중 도서 API 요청 후 로컬 DB 저장 (API 스펙에 BOOKID가 추가되어야함)

@AndroidEntryPoint
class HomeFragment : Fragment() {

	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!
	private val homeViewModel: HomeViewModel by viewModels()
	private lateinit var mainReadingBookAdapter: MainBookAdapter
	private lateinit var channelListDataAdapter: ChannelListDataAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
		binding.lifecycleOwner = this
		binding.viewmodel = homeViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRecyclerView()
		observeUiState()
		observePagingReadingBookData()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		homeViewModel.uiState.collect { uiState ->
			channelListDataAdapter.submitList(uiState.channels)
			uiState.readingBooks
		}
	}

	private fun initAdapter() {
		initBookAdapter()
		initChatRoomAdapter()
	}

	private fun initRecyclerView() {
		initBookRcv()
		initChatRoomRcv()
	}

	private fun initBookAdapter() {
		val bookItemClickListener = object : MainBookAdapter.OnItemClickListener {
			override fun onItemClick(bookShelfDataItem: BookShelfDataItem) {
				//도서 누르면 독서중 탭으로 이동
			}
		}
		mainReadingBookAdapter = MainBookAdapter()
		mainReadingBookAdapter.setItemClickListener(bookItemClickListener)
	}

	private fun initChatRoomAdapter() {
		val chatRoomItemClickListener = object : ChannelListDataAdapter.OnItemClickListener {
			override fun onItemClick(channel: Channel) {
				val intent = Intent(requireContext(), ChannelActivity::class.java)
				intent.putExtra(ChannelListFragment.EXTRA_CHAT_ROOM_ID, channel.roomId)
				startActivity(intent)
			}
		}
		channelListDataAdapter = ChannelListDataAdapter()
		channelListDataAdapter.setItemClickListener(chatRoomItemClickListener)
	}

	private fun initBookRcv() {
		with(binding) {
			readingBookRcvMain.adapter = mainReadingBookAdapter
			readingBookRcvMain.addItemDecoration(MainBookItemDecoration())
			readingBookRcvMain.layoutManager =
				LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
		}
	}

	private fun initChatRoomRcv() {
		with(binding) {
			chatRoomUserInRcv.adapter = channelListDataAdapter
			chatRoomUserInRcv.layoutManager =
				LinearLayoutManager(requireContext())
		}
	}

	//도서도 LocalDB에 저장할 수 있게 BookID가 추가되면 좋을 듯함
	private fun observePagingReadingBookData() = lifecycleScope.launch {
		val firstPage = homeViewModel.readingBookResult.first()
		mainReadingBookAdapter.submitData(firstPage)
	}

}