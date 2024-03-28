package com.example.bookchat.ui.home

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
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentHomeBinding
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.channel.ChannelActivity
import com.example.bookchat.ui.channelList.ChannelListFragment
import com.example.bookchat.ui.channelList.adpater.ChannelListDataAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 독서중 도서 API 요청 후 로컬 DB 저장 (API 스펙에 BOOKID가 추가되어야함)

@AndroidEntryPoint
class HomeFragment : Fragment() {

	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!
	private val homeViewModel: HomeViewModel by viewModels()

	@Inject
	lateinit var mainReadingBookAdapter: MainBookAdapter

	@Inject
	lateinit var channelListDataAdapter: ChannelListDataAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = homeViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRecyclerView()
		observeUiEvent()
		observeUiState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		homeViewModel.eventFlow.collect(::handleEvent)
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		homeViewModel.uiState.collect { uiState ->
			mainReadingBookAdapter.submitList(uiState.readingBookShelfBooks)
			channelListDataAdapter.submitList(uiState.channels)
			setEmptyUiVisibility(uiState.readingBookShelfBooks, uiState.channels)
		}
	}

	private fun setEmptyUiVisibility(bookItems: List<BookShelfListItem>, channels: List<Channel>) {
		binding.emptyReadingBookLayout.visibility =
			if (bookItems.isEmpty()) View.VISIBLE else View.INVISIBLE
		binding.emptyChatRoomLayout.visibility =
			if (channels.isEmpty()) View.VISIBLE else View.INVISIBLE
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
		mainReadingBookAdapter.onItemClick = { itemPosition ->
			homeViewModel.onBookItemClick(mainReadingBookAdapter.currentList[itemPosition].bookShelfId)
		}
	}

	private fun initChatRoomAdapter() {
		channelListDataAdapter.onItemClick = { itemPosition ->
			homeViewModel.onChannelItemClick(channelListDataAdapter.currentList[itemPosition].roomId)
		}
	}

	private fun initBookRcv() {
		with(binding.readingBookRcvMain) {
			adapter = mainReadingBookAdapter
			addItemDecoration(MainBookItemDecoration())
			layoutManager =
				LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
		}
	}

	private fun initChatRoomRcv() {
		with(binding.chatRoomUserInRcv) {
			adapter = channelListDataAdapter
			layoutManager = LinearLayoutManager(requireContext())
		}
	}

	private fun moveToReadingBookShelf(bookShelfListItemId: Long) {
		//TODO : ReadingBookShelfFragment로 Fragment 전환
	}

	private fun moveToChannel(channelId: Long) {
		val intent = Intent(requireContext(), ChannelActivity::class.java)
		intent.putExtra(ChannelListFragment.EXTRA_CHAT_ROOM_ID, channelId)
		startActivity(intent)
	}

	private fun handleEvent(event: HomeUiEvent) {
		when (event) {
			is HomeUiEvent.MoveToChannel -> moveToChannel(event.channelId)
			is HomeUiEvent.MoveToReadingBookShelf -> moveToReadingBookShelf(event.bookShelfListItemId)
		}
	}

}