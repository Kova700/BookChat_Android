package com.example.bookchat.ui.fragment

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
import com.example.bookchat.adapter.userchatroomlist.UserChatRoomListDataAdapter
import com.example.bookchat.adapter.wishbookshelf.WishBookShelfDataAdapter
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.databinding.FragmentHomeBinding
import com.example.bookchat.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var mainReadingBookAdapter: WishBookShelfDataAdapter
    private lateinit var userChatRoomListAdapter: UserChatRoomListDataAdapter

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
        observePagingData()

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
        val chatRoomItemClickListener = object : UserChatRoomListDataAdapter.OnItemClickListener {
            override fun onItemClick(userChatRoomListItem: UserChatRoomListItem) {
                //채팅 누르면 채팅방 들어가고,
            }
        }
        userChatRoomListAdapter = UserChatRoomListDataAdapter()
        userChatRoomListAdapter.setItemClickListener(chatRoomItemClickListener)
    }

    private fun initBookRcv() {
        with(binding){
            readingBookRcvMain.adapter = mainReadingBookAdapter
            readingBookRcvMain.addItemDecoration(MainBookItemDecoration())
            readingBookRcvMain.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
    }

    private fun initChatRoomRcv() {
        with(binding){
            chatRoomUserInRcv.adapter = userChatRoomListAdapter
            chatRoomUserInRcv.layoutManager =
                LinearLayoutManager(requireContext())
        }
    }

    private fun observePagingReadingBookData() = lifecycleScope.launch {
        val firstPage = homeViewModel.readingBookResult.first()
        mainReadingBookAdapter.submitData(firstPage)
    }

    private fun observePagingData() = lifecycleScope.launch {
        val firstPage = homeViewModel.chatRoomPagingData.first()
        userChatRoomListAdapter.submitData(firstPage)
    }
}