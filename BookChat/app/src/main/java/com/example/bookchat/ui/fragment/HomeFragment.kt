package com.example.bookchat.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.MainBookItemDecoration
import com.example.bookchat.R
import com.example.bookchat.adapter.home.MainUserChatRoomListAdapter
import com.example.bookchat.adapter.wishbookshelf.WishBookShelfDataAdapter
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.databinding.FragmentHomeBinding
import com.example.bookchat.ui.activity.ChatRoomActivity
import com.example.bookchat.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var mainReadingBookAdapter: WishBookShelfDataAdapter
    private lateinit var mainUserChatRoomListAdapter: MainUserChatRoomListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        with(binding) {
            lifecycleOwner = this@HomeFragment
            viewmodel = homeViewModel
        }
        initAdapter()
        initRecyclerView()
        observePagingReadingBookData()
        observePagingChatRoomData()
        observeReadingBookLoadStateFlow()

        return binding.root
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
        val bookItemClickListener = object : WishBookShelfDataAdapter.OnItemClickListener {
            override fun onItemClick(bookShelfDataItem: BookShelfDataItem) {
                //도서 누르면 독서중 탭으로 이동
            }
        }
        mainReadingBookAdapter = WishBookShelfDataAdapter()
        mainReadingBookAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initChatRoomAdapter() {
        val chatRoomItemClickListener = object : MainUserChatRoomListAdapter.OnItemClickListener {
            override fun onItemClick(chatRoomEntity: ChatRoomEntity) {
                val intent = Intent(requireContext(), ChatRoomActivity::class.java)
                intent.putExtra(ChatRoomListFragment.EXTRA_CHAT_ROOM_LIST_ITEM, chatRoomEntity)
                startActivity(intent)
            }
        }
        mainUserChatRoomListAdapter = MainUserChatRoomListAdapter()
        mainUserChatRoomListAdapter.setItemClickListener(chatRoomItemClickListener)
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
            chatRoomUserInRcv.adapter = mainUserChatRoomListAdapter
            chatRoomUserInRcv.layoutManager =
                LinearLayoutManager(requireContext())
        }
    }

    //도서도 LocalDB에 저장할 수 있게 BookID가 추가되면 좋을 듯함
    private fun observePagingReadingBookData() = lifecycleScope.launch {
        val firstPage = homeViewModel.readingBookResult.first()
        mainReadingBookAdapter.submitData(firstPage)
    }

    private fun observePagingChatRoomData() = lifecycleScope.launch {
        homeViewModel.chatRoomFlow.collect { list ->
            mainUserChatRoomListAdapter.submitList(list)
            val isListEmpty = mainUserChatRoomListAdapter.itemCount == 0
            binding.emptyChatRoomLayout.isVisible = isListEmpty
        }
    }

    //메인에서 독서중 도서, 채팅방 목록 load시에 Paging이 필요하지 않음으로 그냥 ListAdapter로 수정
    //매번 서버로부터 데이터 로드하고, 로컬에서는 캐시데이터를 보여주는 느낌으로
    private fun observeReadingBookLoadStateFlow() = lifecycleScope.launch {
        mainReadingBookAdapter.loadStateFlow.collect { loadState ->
            val isListEmpty = loadState.refresh is LoadState.NotLoading &&
                    mainReadingBookAdapter.itemCount == 0
            binding.emptyReadingBookLayout.isVisible = isListEmpty
        }
    }
}