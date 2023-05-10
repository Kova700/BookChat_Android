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
import com.example.bookchat.adapter.home.MainUserChatRoomListAdapter
import com.example.bookchat.adapter.userchatroomlist.UserChatRoomListDataAdapter
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

    //TODO : 서재 LocalDB 저장해온 걸 가져오는 형식으로 수정
    // LocalDB에 없다면 특정 개수만 서버에 가져오는 방식으로 RemoteMediator 연결
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
                intent.putExtra(
                    ChatRoomListFragment.EXTRA_CHAT_ROOM_LIST_ITEM,
                    chatRoomEntity.toUserChatRoomListItem()
                )
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

    private fun observePagingReadingBookData() = lifecycleScope.launch {
        val firstPage = homeViewModel.readingBookResult.first()
        mainReadingBookAdapter.submitData(firstPage)
    }

    private fun observePagingChatRoomData() = lifecycleScope.launch {
        homeViewModel.chatRoomFlow.collect { list ->
            mainUserChatRoomListAdapter.submitList(list)
        }
    }
}