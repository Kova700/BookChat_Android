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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.userchatroomlist.UserChatRoomListDataAdapter
import com.example.bookchat.adapter.userchatroomlist.UserChatRoomListHeaderAdapter
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.databinding.FragmentChatRoomListBinding
import com.example.bookchat.ui.activity.MakeChatRoomActivity
import com.example.bookchat.viewmodel.ChatRoomListViewModel
import com.example.bookchat.viewmodel.ChatRoomListViewModel.ChatRoomListUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/*상단 고정, 채팅방 알림 끄기도 구현해야함*/
/*꾹 눌러서 설정창 띄우기*/
/*우측으로 밀어서 설정창 보이기*/
@AndroidEntryPoint
class ChatRoomListFragment : Fragment() {

    private lateinit var binding: FragmentChatRoomListBinding
    private lateinit var userChatRoomListHeaderAdapter: UserChatRoomListHeaderAdapter
    private lateinit var userChatRoomListDataAdapter: UserChatRoomListDataAdapter
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
        observeUiEvent()

        return binding.root
    }

    private fun observeUiEvent() = lifecycleScope.launch{
        chatRoomListViewModel.eventFlow.collect{ event -> handleEvent(event) }
    }

    private fun observePagingData() = lifecycleScope.launch{
        chatRoomListViewModel.chatRoomPagingData.collect{ pagingData ->
            userChatRoomListDataAdapter.submitData(pagingData)
        }
    }

    private fun initAdapter() {
        //롱클릭 리스너도 설정해줘야함
        val chatRoomItemClickListener = object : UserChatRoomListDataAdapter.OnItemClickListener {
            override fun onItemClick(userChatRoomListItem: UserChatRoomListItem) {
                //채팅 누르면 채팅방 들어가고,
            }
        }
        userChatRoomListHeaderAdapter = UserChatRoomListHeaderAdapter()
        userChatRoomListDataAdapter = UserChatRoomListDataAdapter()
        userChatRoomListDataAdapter.setItemClickListener(chatRoomItemClickListener)
    }

    private fun initRecyclerView() {
        with(binding) {
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig,
                userChatRoomListHeaderAdapter,
                userChatRoomListDataAdapter
            )
            chatRcv.adapter = concatAdapter
            chatRcv.setHasFixedSize(true)
            chatRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun handleEvent(event : ChatRoomListUiEvent) = when(event){
        ChatRoomListUiEvent.MoveToMakeChatRoomPage -> {
            val intent = Intent(requireContext(), MakeChatRoomActivity::class.java)
            startActivity(intent)
        }
        ChatRoomListUiEvent.MoveToSearchChatRoomPage -> {}
    }

}