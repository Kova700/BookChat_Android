package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.chatroomlist.ChatRoomListDataAdapter
import com.example.bookchat.adapter.chatroomlist.ChatRoomListHeaderAdapter
import com.example.bookchat.databinding.FragmentChatRoomListBinding
import com.example.bookchat.viewmodel.ChatRoomListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/*상단 고정, 채팅방 알림 끄기도 구현해야함*/
/*꾹 눌러서 설정창 띄우기*/
/*우측으로 밀어서 설정창 보이기*/
@AndroidEntryPoint
class ChatRoomListFragment : Fragment() {

    private lateinit var binding: FragmentChatRoomListBinding
    private lateinit var chatRoomListHeaderAdapter: ChatRoomListHeaderAdapter
    private lateinit var chatRoomListDataAdapter: ChatRoomListDataAdapter
    private val chatRoomListViewModel: ChatRoomListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_room_list, container, false)
        binding.viewmodel = chatRoomListViewModel
        initAdapter()
        initRecyclerView()
        observePagingData()

        return binding.root
    }

    private fun observePagingData() = lifecycleScope.launch{
        chatRoomListViewModel.chatRoomPagingData.collect{ pagingData ->
            chatRoomListDataAdapter.submitData(pagingData)
        }
    }

    private fun initAdapter() {
        chatRoomListHeaderAdapter = ChatRoomListHeaderAdapter()
        chatRoomListDataAdapter = ChatRoomListDataAdapter()
        //클릭리스너 설정해줘야함
    }

    private fun initRecyclerView() {
        with(binding) {
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig,
                chatRoomListHeaderAdapter,
                chatRoomListDataAdapter
            )
            chatRcv.adapter = concatAdapter
            chatRcv.setHasFixedSize(true)
            chatRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

}